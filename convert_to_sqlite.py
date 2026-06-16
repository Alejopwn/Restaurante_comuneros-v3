import re

with open('db_dump.sql', 'r', encoding='utf-8') as f:
    sql = f.read()

# Extract INSERT statements
inserts = re.findall(r'INSERT INTO `(\w+)` VALUES \((.*?)\);', sql)
# Actually, the original inserts have multiple values: `INSERT INTO table VALUES (...),(...);`
# It's easier to just grab the raw lines starting with `INSERT INTO`
insert_lines = [line for line in sql.split('\n') if line.startswith('INSERT INTO')]

schema = """
CREATE TABLE config (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  ruc TEXT NOT NULL,
  nombre TEXT NOT NULL,
  telefono TEXT NOT NULL,
  direccion TEXT NOT NULL,
  mensaje TEXT NOT NULL
);

CREATE TABLE salas (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  nombre TEXT NOT NULL,
  mesas INTEGER NOT NULL
);

CREATE TABLE usuarios (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  nombre TEXT NOT NULL,
  correo TEXT NOT NULL,
  pass TEXT NOT NULL,
  rol TEXT NOT NULL
);

CREATE TABLE platos (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  nombre TEXT NOT NULL,
  precio REAL NOT NULL,
  fecha TEXT DEFAULT NULL
);

CREATE TABLE pedidos (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  id_sala INTEGER NOT NULL,
  num_mesa INTEGER NOT NULL,
  fecha TEXT DEFAULT CURRENT_TIMESTAMP,
  total REAL NOT NULL,
  estado TEXT CHECK(estado IN ('PENDIENTE','FINALIZADO')) NOT NULL DEFAULT 'PENDIENTE',
  usuario TEXT NOT NULL,
  tipo_pago TEXT DEFAULT NULL,
  fecha_registro TEXT DEFAULT NULL,
  FOREIGN KEY (id_sala) REFERENCES salas (id)
);

CREATE TABLE detalle_pedidos (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  nombre TEXT NOT NULL,
  precio REAL NOT NULL,
  cantidad INTEGER NOT NULL,
  comentario TEXT,
  id_pedido INTEGER NOT NULL,
  FOREIGN KEY (id_pedido) REFERENCES pedidos (id)
);

CREATE TABLE historial_ventas (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  id_pedido INTEGER DEFAULT NULL,
  fecha TEXT DEFAULT NULL,
  total REAL DEFAULT NULL,
  tipo_pago TEXT DEFAULT NULL,
  usuario TEXT DEFAULT NULL,
  FOREIGN KEY (id_pedido) REFERENCES pedidos (id)
);
"""

# replace backticks in insert_lines
cleaned_inserts = []
for line in insert_lines:
    line = line.replace('`', '')
    cleaned_inserts.append(line)

with open('schema_sqlite.sql', 'w', encoding='utf-8') as f:
    f.write(schema)
    f.write('\n')
    for line in cleaned_inserts:
        f.write(line + '\n')
