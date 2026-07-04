param(
  [string]$DatabaseName = "esukarelawan",
  [string]$AppUser = "root",
  [string]$AppPassword = "",
  [int]$Port = 3307,
  [string]$MysqlExe = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path -LiteralPath $MysqlExe)) {
  throw "MySQL client was not found at: $MysqlExe"
}

$schemaPath = Join-Path $PSScriptRoot "..\src\main\resources\database-schema.sql"
if (-not (Test-Path -LiteralPath $schemaPath)) {
  throw "Database schema was not found at: $schemaPath"
}

$rootPassword = Read-Host "Enter MySQL root password, or press Enter if NetBeans uses blank root password" -AsSecureString
$rootPlain = [Runtime.InteropServices.Marshal]::PtrToStringBSTR(
  [Runtime.InteropServices.Marshal]::SecureStringToBSTR($rootPassword)
)

$bootstrapSql = @"
CREATE DATABASE IF NOT EXISTS $DatabaseName CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
"@

$tempSql = Join-Path ([System.IO.Path]::GetTempPath()) "esukarelawan-create-db.sql"
$tempSchema = Join-Path ([System.IO.Path]::GetTempPath()) "esukarelawan-schema.sql"
Set-Content -LiteralPath $tempSql -Value $bootstrapSql -Encoding UTF8
Copy-Item -LiteralPath $schemaPath -Destination $tempSchema -Force

try {
  $rootArgs = @("--protocol=tcp", "--host=localhost", "--port=$Port", "-uroot")
  if (-not [string]::IsNullOrEmpty($rootPlain)) {
    $rootArgs += "-p$rootPlain"
  }
  & $MysqlExe @rootArgs --execute="source $tempSql"
  if ($LASTEXITCODE -ne 0) { throw "Could not create database or application user." }

  $appArgs = @("--protocol=tcp", "--host=localhost", "--port=$Port", "-u$AppUser")
  if (-not [string]::IsNullOrEmpty($AppPassword)) {
    $appArgs += "-p$AppPassword"
  }
  & $MysqlExe @appArgs $DatabaseName --execute="source $tempSchema"
  if ($LASTEXITCODE -ne 0) { throw "Could not import database schema." }

  Write-Host ""
  Write-Host "E-Sukarelawan database created successfully."
  Write-Host "Database: $DatabaseName"
  Write-Host "User: $AppUser"
  Write-Host "Password: $AppPassword"
  Write-Host ""
  Write-Host "Use these app settings:"
  Write-Host "ESUKARELAWAN_DB_URL=jdbc:mysql://localhost:$Port/$DatabaseName"
  Write-Host "ESUKARELAWAN_DB_USER=$AppUser"
  Write-Host "ESUKARELAWAN_DB_PASSWORD=$AppPassword"

  [Environment]::SetEnvironmentVariable("ESUKARELAWAN_DB_URL", "jdbc:mysql://localhost:$Port/$DatabaseName", "User")
  [Environment]::SetEnvironmentVariable("ESUKARELAWAN_DB_USER", $AppUser, "User")
  if ([string]::IsNullOrEmpty($AppPassword)) {
    [Environment]::SetEnvironmentVariable("ESUKARELAWAN_DB_PASSWORD", $null, "User")
  } else {
    [Environment]::SetEnvironmentVariable("ESUKARELAWAN_DB_PASSWORD", $AppPassword, "User")
  }
  Write-Host ""
  Write-Host "Windows user environment variables were saved. Restart your Jakarta server after running this."
} finally {
  Remove-Item -LiteralPath $tempSql -Force -ErrorAction SilentlyContinue
  Remove-Item -LiteralPath $tempSchema -Force -ErrorAction SilentlyContinue
  if ($rootPlain) {
    $rootPlain = $null
  }
}
