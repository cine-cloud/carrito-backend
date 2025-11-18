<#
Script PowerShell para generar un archivo SQL (a partir de la plantilla) que inserta
2 usuarios y un carrito con items tomados desde la BD de `peliculas`.

Requisitos:
- Docker disponible y contenedores en marcha.
- Contenedor Postgres de peliculas llamado: peliculas-db-container
- Contenedor Postgres de carritos llamado: carritos-db-container
- Credenciales por defecto usadas en los compose: user_peliculas / pass_peliculas y user_carritos / pass_carritos

Uso (desde PowerShell):
Set-Location 'c:\Users\katii\workspaces\carrito-backend\scripts'
.\generate_and_apply_carrito.ps1 -PeliculaA 'Inception' -PeliculaB 'The Dark Knight'

El script generará `..\bd\data_carrito.sql`, lo copiará al contenedor `carritos-db-container`
y lo ejecutará contra la BD `carritos_db`.
#>

param(
    [string]$PeliculaA = 'Inception',
    [string]$PeliculaB = 'The Dark Knight',
    [string]$User1 = 'user_test_1',
    [string]$User2 = 'user_test_2'
)

Set-StrictMode -Version Latest

$scriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Definition
$repoRoot = Resolve-Path (Join-Path $scriptRoot '..')
$bdDir = Join-Path $repoRoot 'bd'
$templatePath = Join-Path $bdDir 'data_carrito_template.sql'
$outPath = Join-Path $bdDir 'data_carrito.sql'

function Escape-SqlString([string]$s){
    return $s -replace "'","''"
}

Write-Host "Generando SQL usando peliculas: '$PeliculaA' y '$PeliculaB'..."

# Helper: consulta a peliculas-db-container para obtener id|titulo|precio
function Get-PeliculaInfo([string]$titulo){
    $safe = $titulo.Replace("'","''")
    $cmd = "SELECT pelicula_id||'|'||replace(titulo,E'\\n',' ')||'|'||precio FROM peliculas WHERE titulo = '$safe' LIMIT 1;"
    $out = docker exec peliculas-db-container psql -U user_peliculas -d peliculas_db -t -A -F '|' -c "$cmd" 2>$null
    if (-not $out) { return $null }
    $parts = $out.Trim() -split '\|'
    return @{ id = $parts[0].Trim(); titulo = $parts[1].Trim(); precio = $parts[2].Trim() }
}

$infoA = Get-PeliculaInfo -titulo $PeliculaA
if (-not $infoA) { Write-Error "No se encontró la pelicula '$PeliculaA' en peliculas_db"; exit 1 }
$infoB = Get-PeliculaInfo -titulo $PeliculaB
if (-not $infoB) { Write-Error "No se encontró la pelicula '$PeliculaB' en peliculas_db"; exit 1 }

Write-Host "Pelicula A: $($infoA.id) - $($infoA.titulo) - $($infoA.precio)"
Write-Host "Pelicula B: $($infoB.id) - $($infoB.titulo) - $($infoB.precio)"

# Generar ids GUID
$cartId = [guid]::NewGuid().ToString()
$itemA = [guid]::NewGuid().ToString()
$itemB = [guid]::NewGuid().ToString()

# Construir los INSERTs para ITEMS (escape de strings)
$tA = Escape-SqlString $infoA.titulo
$tB = Escape-SqlString $infoB.titulo

$itemsSql = @()
$itemsSql += "INSERT INTO carrito_items (item_id, carrito_id, pelicula_id, titulo_snapshot, precio_unitario, cantidad) VALUES ('$itemA', '$cartId', $($infoA.id), '$tA', $($infoA.precio), 1) ON CONFLICT (item_id) DO NOTHING;"
$itemsSql += "INSERT INTO carrito_items (item_id, carrito_id, pelicula_id, titulo_snapshot, precio_unitario, cantidad) VALUES ('$itemB', '$cartId', $($infoB.id), '$tB', $($infoB.precio), 1) ON CONFLICT (item_id) DO NOTHING;"

# Leer plantilla y reemplazar tokens
if (-not (Test-Path $templatePath)) { Write-Error "No existe la plantilla: $templatePath"; exit 1 }
$template = Get-Content $templatePath -Raw
$final = $template -replace '\{\{USER1\}\}',$User1 -replace '\{\{USER2\}\}',$User2 -replace '\{\{CART_ID\}\}',$cartId -replace '\{\{ITEMS\}\}', ($itemsSql -join "`n")

# Escribir archivo SQL final
Set-Content -Path $outPath -Value $final -Encoding UTF8
Write-Host "Archivo SQL generado: $outPath"

# Copiar y ejecutar en el contenedor carritos-db-container
Write-Host "Copiando al contenedor 'carritos-db-container' y ejecutando..."
docker cp $outPath carritos-db-container:/tmp/data_carrito.sql
docker exec -i carritos-db-container psql -U user_carritos -d carritos_db -f /tmp/data_carrito.sql

if ($LASTEXITCODE -eq 0) {
    Write-Host "SQL ejecutado correctamente. Carrito: $cartId"
    Write-Host "Usuarios creados: $User1, $User2"
    Write-Host "Items insertados: $itemA, $itemB"
} else {
    Write-Error "Fallo al ejecutar el SQL en carritos-db-container"
}
