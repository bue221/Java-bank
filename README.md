# Java Bank Transfer (Consola)

Aplicación simple en Java para simular una transferencia bancaria **atómica** entre dos cuentas que viven en **dos bases SQLite diferentes** (`origin.db` y `destination.db`).

## ¿Qué resuelve este proyecto?
- Conexión simultánea a 2 bases de datos.
- Transferencia bancaria entre cuenta origen y cuenta destino.
- Confirmación total o reversión total ante fallos (comportamiento atómico).
- Manejo básico de excepciones para entradas inválidas y errores SQL.

## Requisitos locales
- Java 17+
- Maven 3.9+

Verifica versiones:
```bash
java -version
mvn -version
```

## Estructura
- `Main.java`: flujo por consola, lectura del monto y orquestación.
- `DatabaseInitializer.java`: creación de tabla y datos semilla.
- `TransferService.java`: lógica transaccional de débito/crédito con rollback.

## Cómo correr en local
1. Clona el repositorio:
   ```bash
   git clone <URL_DEL_REPO>
   cd Java-bank
   ```
2. Compila:
   ```bash
   mvn clean compile
   ```
3. Ejecuta la app:
   ```bash
   mvn exec:java
   ```
4. Ingresa un monto (ejemplo: `150`).

## Ejemplo esperado
- Saldo inicial origen: `1000.0`
- Saldo inicial destino: `500.0`
- Si transfieres `150`:
    - origen queda en `850.0`
    - destino queda en `650.0`

## Notas
- Cuenta origen: id `1` en `origin.db` con saldo inicial `1000`.
- Cuenta destino: id `2` en `destination.db` con saldo inicial `500`.
- Las bases `origin.db` y `destination.db` se crean automáticamente al ejecutar por primera vez.
