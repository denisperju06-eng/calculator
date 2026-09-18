import os
import glob
import subprocess

labs = ['lab3', 'lab4', 'lab5']

for lab in labs:
    src_dir = os.path.join(lab, 'src')
    if not os.path.exists(src_dir):
        continue
    
    core_dir = os.path.join(src_dir, 'core')
    os.makedirs(core_dir, exist_ok=True)
    
    java_files = glob.glob(os.path.join(src_dir, '*.java'))
    main_files = []
    core_files = []
    
    for f in java_files:
        with open(f, 'r') as file:
            content = file.read()
            if 'public static void main' in content:
                main_files.append(f)
            else:
                core_files.append(f)
                
    for f in core_files:
        new_path = os.path.join(core_dir, os.path.basename(f))
        os.rename(f, new_path)
        
        with open(new_path, 'r') as file:
            content = file.read()
        
        # Add package declaration
        if not content.startswith('package '):
            with open(new_path, 'w') as file:
                file.write('package core;\n' + content)
                
    for f in main_files:
        with open(f, 'r') as file:
            content = file.read()
            
        # Add import core.*;
        if 'import core.*;' not in content:
            with open(f, 'w') as file:
                file.write('import core.*;\n' + content)
                
    print(f"Refactored {lab}")
    
    # Recompile
    print(f"Compiling {lab}...")
    compile_cmd = f"javac -d {lab}/out {lab}/src/*.java {lab}/src/core/*.java"
    res = subprocess.run(compile_cmd, shell=True, capture_output=True, text=True)
    if res.returncode != 0:
        print(f"Failed to compile {lab}:\n{res.stderr}")
    else:
        print(f"Successfully compiled {lab}")

