param(
    [int]$Port = 0,
    [switch]$NoOpenReport
)

$ErrorActionPreference = 'Stop'
$PlaywrightDir = $PSScriptRoot
$ProjectRoot = Split-Path -Parent $PlaywrightDir
$ResultsDir = Join-Path $PlaywrightDir 'results'
$ReportPath = Join-Path $ResultsDir 'eims-additional-report.html'
$StdoutLog = Join-Path $ResultsDir 'spring-boot.out.log'
$StderrLog = Join-Path $ResultsDir 'spring-boot.err.log'
$SpringProcess = $null
$ExitCode = 1
$TestTotal = 47

[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new($false)
$OutputEncoding = [Console]::OutputEncoding

function Get-AvailableTcpPort {
    $Listener = [System.Net.Sockets.TcpListener]::new([System.Net.IPAddress]::Loopback, 0)
    try {
        $Listener.Start()
        return ([System.Net.IPEndPoint]$Listener.LocalEndpoint).Port
    }
    finally {
        $Listener.Stop()
    }
}

function Remove-DiagnosticEmployees {
    $PropertiesPath = Join-Path $ProjectRoot 'src\main\resources\application.properties'
    if (-not (Test-Path -LiteralPath $PropertiesPath)) { return }

    $Properties = @{}
    foreach ($Line in Get-Content -LiteralPath $PropertiesPath -Encoding UTF8) {
        if ($Line -match '^\s*([^#=]+?)\s*=\s*(.*)$') {
            $Properties[$Matches[1]] = $Matches[2]
        }
    }

    $DatasourceUrl = $Properties['spring.datasource.url']
    if ($DatasourceUrl -notmatch '^jdbc:mysql://(?<host>[^/:?]+)(?::(?<port>\d+))?/(?<database>[^?]+)') { return }

    $MysqlCommand = Get-Command mysql.exe -ErrorAction SilentlyContinue
    if ($null -eq $MysqlCommand) {
        $DefaultMysqlPath = Join-Path $env:ProgramFiles 'MySQL\MySQL Server 8.0\bin\mysql.exe'
        if (-not (Test-Path -LiteralPath $DefaultMysqlPath)) { return }
        $MysqlPath = $DefaultMysqlPath
    }
    else {
        $MysqlPath = $MysqlCommand.Source
    }

    $HostName = $Matches['host']
    $DatabaseName = $Matches['database']
    $DatabasePort = if ($Matches['port']) { $Matches['port'] } else { '3306' }
    $PreviousMysqlPassword = $env:MYSQL_PWD
    try {
        $env:MYSQL_PWD = $Properties['spring.datasource.password']
        $Sql = "DELETE FROM employee WHERE emp_no > 10020 AND last_name LIKE '診断%' AND first_name = '太郎' AND password = 'pass1234'"
        & $MysqlPath --host=$HostName --port=$DatabasePort --user=$($Properties['spring.datasource.username']) --database=$DatabaseName --default-character-set=utf8mb4 --execute=$Sql 2>$null
    }
    catch {
        Write-Host '診断用データの後片付けはスキップしました。' -ForegroundColor Yellow
    }
    finally {
        if ($null -eq $PreviousMysqlPassword) {
            Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
        }
        else {
            $env:MYSQL_PWD = $PreviousMysqlPassword
        }
    }
}

function Write-DashboardProgress {
    param(
        [int]$Percent,
        [string]$Phase,
        [string]$Message
    )

    $Payload = @{ percent = $Percent; phase = $Phase; message = $Message } | ConvertTo-Json -Compress
    Write-Output "@@EIMS@@$Payload"
    Write-Host ("[{0,3}%] {1}" -f $Percent, $Message) -ForegroundColor Cyan
}

function Write-FriendlyFailureReport {
    param(
        [string]$Title,
        [string]$Message,
        [string]$Detail
    )

    New-Item -ItemType Directory -Force -Path $ResultsDir | Out-Null
    $EncodedTitle = [System.Net.WebUtility]::HtmlEncode($Title)
    $EncodedMessage = [System.Net.WebUtility]::HtmlEncode($Message)
    $EncodedDetail = [System.Net.WebUtility]::HtmlEncode($Detail)
    $Html = @"
<!doctype html><html lang="ja"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>EIMS 追加機能 総合診断レポート</title><style>
body{margin:0;background:#f4f7fa;color:#1f2937;font-family:"Yu Gothic UI","Meiryo",sans-serif;line-height:1.75}header{background:linear-gradient(135deg,#102b45,#285d8d);color:#fff;padding:40px 24px 70px}main,header div{width:min(900px,calc(100% - 32px));margin:auto}main{margin-top:-40px}.card{background:#fff;border-radius:18px;padding:30px;box-shadow:0 12px 30px #17324d20;border-left:7px solid #c43232}.badge{display:inline-block;background:#fee2e2;color:#a51d1d;border-radius:99px;padding:7px 15px;font-weight:800}h1{margin:0}h2{margin:16px 0 5px}.hint{background:#eff6ff;border-radius:12px;padding:14px 18px;margin:20px 0}details{margin-top:18px}pre{white-space:pre-wrap;overflow:auto;background:#111827;color:#e5e7eb;padding:18px;border-radius:10px;font:12px/1.6 Consolas,monospace}
</style></head><body><header><div><h1>EIMS 追加機能 総合診断レポート</h1><p>追加機能・総合診断</p><p>この診断レポートは100%正確とは限りません。実際の動作やテスト結果と差異がある場合は、講師へ確認してください。</p></div></header><main><section class="card"><span class="badge">テストを開始できませんでした</span><h2>$EncodedTitle</h2><p>$EncodedMessage</p><div class="hint"><strong>まず確認してください</strong><ul><li>MySQLが起動しているか</li><li>追加機能用DBSETが完了しているか</li><li>Java 17とNode.jsがインストールされているか</li><li>Spring Bootのコンパイルエラーがないか</li></ul></div><details open><summary>技術的な詳細を見る</summary><pre>$EncodedDetail</pre></details></section></main></body></html>
"@
    Set-Content -LiteralPath $ReportPath -Value $Html -Encoding UTF8
}

try {
    Write-DashboardProgress 2 '準備' '実行環境を確認しています...'
    New-Item -ItemType Directory -Force -Path $ResultsDir | Out-Null
    Remove-Item -LiteralPath $StdoutLog, $StderrLog -Force -ErrorAction SilentlyContinue

    if (-not (Get-Command node.exe -ErrorAction SilentlyContinue)) {
        throw 'Node.jsが見つかりません。Node.jsのLTS版をインストールしてください。'
    }
    if (-not (Get-Command npm.cmd -ErrorAction SilentlyContinue)) {
        throw 'npmが見つかりません。Node.jsを再インストールしてください。'
    }

    Push-Location $PlaywrightDir
    try {
        if (-not (Test-Path (Join-Path $PlaywrightDir 'node_modules\@playwright\test'))) {
            Write-DashboardProgress 5 '初回準備' 'Playwrightをインストールしています...'
            Write-Host '初回準備：Playwrightをインストールしています...' -ForegroundColor Cyan
            & npm.cmd ci
            if ($LASTEXITCODE -ne 0) { throw 'Playwrightのインストールに失敗しました。インターネット接続を確認してください。' }
        }

        Write-DashboardProgress 10 'ブラウザー準備' 'テスト用ブラウザーを確認しています...'
        & npx.cmd playwright install chromium
        if ($LASTEXITCODE -ne 0) { throw 'Chromiumの準備に失敗しました。インターネット接続を確認してください。' }
    }
    finally {
        Pop-Location
    }

    Write-DashboardProgress 15 'データ準備' '前回の診断用データを整理しています...'
    Remove-DiagnosticEmployees

    $MavenWrapper = Join-Path $ProjectRoot 'mvnw.cmd'
    if (-not (Test-Path -LiteralPath $MavenWrapper)) {
        throw 'mvnw.cmdが見つかりません。EIMSプロジェクトのフォルダー構成を確認してください。'
    }

    if ($Port -eq 0) {
        $Port = Get-AvailableTcpPort
    }
    Write-DashboardProgress 20 'ポート選択' "空いているポート $Port を使用します。"
    Write-DashboardProgress 25 'サーバー起動' 'EIMSを起動しています...'
    $PreviousRestartSetting = $env:SPRING_DEVTOOLS_RESTART_ENABLED
    $env:SPRING_DEVTOOLS_RESTART_ENABLED = 'false'
    try {
        $SpringProcess = Start-Process -FilePath $MavenWrapper `
            -ArgumentList @('clean', 'spring-boot:run', "-Dspring-boot.run.arguments=--server.port=$Port") `
            -WorkingDirectory $ProjectRoot `
            -WindowStyle Hidden `
            -RedirectStandardOutput $StdoutLog `
            -RedirectStandardError $StderrLog `
            -PassThru
    }
    finally {
        if ($null -eq $PreviousRestartSetting) {
            Remove-Item Env:SPRING_DEVTOOLS_RESTART_ENABLED -ErrorAction SilentlyContinue
        }
        else {
            $env:SPRING_DEVTOOLS_RESTART_ENABLED = $PreviousRestartSetting
        }
    }

    $BaseUrl = "http://127.0.0.1:$Port"
    $Started = $false
    for ($Attempt = 0; $Attempt -lt 120; $Attempt++) {
        if ($SpringProcess.HasExited) { break }
        try {
            $Response = Invoke-WebRequest -Uri "$BaseUrl/" -UseBasicParsing -TimeoutSec 2
            if ($Response.StatusCode -lt 500) {
                $Started = $true
                break
            }
        }
        catch {
            if ($Attempt -gt 0 -and $Attempt % 10 -eq 0) {
                Write-DashboardProgress 25 'サーバー起動' "EIMSの起動を待っています（$Attempt 秒経過）..."
            }
            Start-Sleep -Seconds 1
        }
    }

    if (-not $Started) {
        $Log = ((Get-Content -LiteralPath $StdoutLog, $StderrLog -ErrorAction SilentlyContinue) -join [Environment]::NewLine)
        Write-FriendlyFailureReport -Title 'EIMSを起動できませんでした' -Message '追加機能の総合テストを開始できません。下のログを確認してください。' -Detail $Log
        throw 'Spring Bootの起動に失敗しました。'
    }

    Write-DashboardProgress 30 '追加機能・総合診断' "EIMSが起動しました。追加機能を利用シナリオに沿って全${TestTotal}件確認します。"
    Push-Location $PlaywrightDir
    try {
        $env:EIMS_BASE_URL = $BaseUrl
        & npx.cmd playwright test tests/additional.spec.ts
        $ExitCode = $LASTEXITCODE
    }
    finally {
        Remove-Item Env:EIMS_BASE_URL -ErrorAction SilentlyContinue
        Pop-Location
    }
}
catch {
    Write-Host $_.Exception.Message -ForegroundColor Red
    if (-not (Test-Path -LiteralPath $ReportPath)) {
        $Detail = $_.Exception.ToString()
        if (Test-Path -LiteralPath $StderrLog) {
            $Detail += [Environment]::NewLine + (Get-Content -LiteralPath $StderrLog -Raw)
        }
        Write-FriendlyFailureReport -Title 'テストの準備中に問題が発生しました' -Message $_.Exception.Message -Detail $Detail
    }
    $ExitCode = 1
}
finally {
    if ($null -ne $SpringProcess -and -not $SpringProcess.HasExited) {
        & taskkill.exe /PID $SpringProcess.Id /T /F 2>$null | Out-Null
    }
    Remove-DiagnosticEmployees
    if ((Test-Path -LiteralPath $ReportPath) -and -not $NoOpenReport) {
        Write-Host '結果報告書をブラウザーで開きます。' -ForegroundColor Green
        Start-Process -FilePath $ReportPath
    }
}

exit $ExitCode
