$ErrorActionPreference = 'Stop'
$DashboardServer = Join-Path $PSScriptRoot 'dashboard-server.mjs'

if (-not (Get-Command node.exe -ErrorAction SilentlyContinue)) {
    Add-Type -AssemblyName PresentationFramework
    [System.Windows.MessageBox]::Show('Node.jsが見つかりません。Node.jsのLTS版をインストールしてください。', 'EIMS 実装診断アプリ', 'OK', 'Error') | Out-Null
    exit 1
}

Start-Process -FilePath 'node.exe' -ArgumentList @($DashboardServer) -WorkingDirectory $PSScriptRoot -WindowStyle Hidden
