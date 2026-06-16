import os
import re

dao_files = [
    'src/Modelo/SalasDao.java',
    'src/Modelo/PlatosDao.java',
    'src/Modelo/LoginDao.java',
    'src/Modelo/PedidosDao.java',
    'src/Modelo/ImpresionTicket.java'
]

def process_file(filepath):
    if not os.path.exists(filepath):
        return

    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # 1. Comment out class level variables
    content = re.sub(r'(\s+)Connection con;', r'\1// Connection con;', content)
    content = re.sub(r'(\s+)PreparedStatement ps;', r'\1// PreparedStatement ps;', content)
    content = re.sub(r'(\s+)ResultSet rs;', r'\1// ResultSet rs;', content)

    # 2. Replace this.con -> con, this.ps -> ps, this.rs -> rs
    content = content.replace('this.con', 'con')
    content = content.replace('this.ps', 'ps')
    content = content.replace('this.rs', 'rs')

    # 3. Inject local variables at the beginning of every method
    # Method signature regex: public [Type] [MethodName]([args]) {
    # Note: does not handle 'throws' or complex generics perfectly but should work for this project.
    method_pattern = re.compile(r'(public\s+[\w\<\>\[\]]+\s+\w+\s*\([^)]*\)\s*\{)')
    
    def inject_locals(match):
        return match.group(1) + '\n        Connection con = null;\n        PreparedStatement ps = null;\n        ResultSet rs = null;'
    
    content = method_pattern.sub(inject_locals, content)

    # Some specific manual fixes for PedidosDao where date is saved
    if 'PedidosDao.java' in filepath:
        # 1. RegistrarPedido to use datetime('now', 'localtime')
        # Original: INSERT INTO pedidos (id_sala, num_mesa, total, usuario) VALUES (?,?,?,?)
        content = content.replace(
            '"INSERT INTO pedidos (id_sala, num_mesa, total, usuario) VALUES (?,?,?,?)"',
            '"INSERT INTO pedidos (id_sala, num_mesa, total, usuario, fecha) VALUES (?,?,?,?, datetime(\'now\', \'localtime\'))"'
        )
        
        # 2. Fix the BETWEEN queries to use String instead of Timestamp
        content = content.replace('ps.setTimestamp(1, fechaInicio);', 'ps.setString(1, new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(fechaInicio));')
        content = content.replace('ps.setTimestamp(2, fechaFin);', 'ps.setString(2, new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(fechaFin));')
        
    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

for f in dao_files:
    process_file(f)

print("Refactoring complete.")
