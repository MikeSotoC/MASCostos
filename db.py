import sqlite3

conn = sqlite3.connect("SQLDelphin_basica.sqlite")
cursor = conn.cursor()

cursor.execute("SELECT name FROM sqlite_master WHERE type='table';")
tablas = [t[0] for t in cursor.fetchall()]

estructura = {}

for tabla in tablas:
    cursor.execute(f"PRAGMA table_info({tabla});")
    columnas = cursor.fetchall()
    estructura[tabla] = [col[1] for col in columnas]

relaciones = set()

for tabla, columnas in estructura.items():
    for col in columnas:
        if col.startswith("id_"):
            ref = col.replace("id_", "")

            if ref in estructura:
                # ❌ evitar auto-relaciones
                if tabla != ref:
                    relaciones.add(f"{tabla}.{col} → {ref}.{col}")

# imprimir limpio
print("🔗 FLUJO LIMPIO:\n")
for r in sorted(relaciones):
    print(r)

conn.close()