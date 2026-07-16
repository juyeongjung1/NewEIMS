$ErrorActionPreference = 'Stop'
$DashboardServer = Join-Path $PSScriptRoot 'dashboard-server.mjs'
$ResultsDir = Join-Path $PSScriptRoot 'results'
$OutputLog = Join-Path $ResultsDir 'dashboard-launch.out.log'
$ErrorLog = Join-Path $ResultsDir 'dashboard-launch.err.log'

if (-not (Get-Command node.exe -ErrorAction SilentlyContinue)) {
    Add-Type -AssemblyName PresentationFramework
    [System.Windows.MessageBox]::Show('Node.jsが見つかりません。Node.jsのLTS版をインストールしてください。', 'EIMS 追加機能診断アプリ', 'OK', 'Error') | Out-Null
    exit 1
}

New-Item -ItemType Directory -Force -Path $ResultsDir | Out-Null
Remove-Item -LiteralPath $OutputLog, $ErrorLog -Force -ErrorAction SilentlyContinue
$DashboardProcess = Start-Process -FilePath 'node.exe' -ArgumentList @($DashboardServer) -WorkingDirectory $PSScriptRoot -WindowStyle Hidden -RedirectStandardOutput $OutputLog -RedirectStandardError $ErrorLog -PassThru
Start-Sleep -Seconds 2
if ($DashboardProcess.HasExited) {
    $Detail = Get-Content -LiteralPath $ErrorLog -Raw -ErrorAction SilentlyContinue
    Add-Type -AssemblyName PresentationFramework
    [System.Windows.MessageBox]::Show("診断アプリを起動できませんでした。`n`n$Detail", 'EIMS 追加機能診断アプリ', 'OK', 'Error') | Out-Null
    exit 1
}
