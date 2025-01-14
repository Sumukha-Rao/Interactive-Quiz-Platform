import pandas as pd
from sqlalchemy import create_engine
import sys
import os

def is_excel_file(file_path):
    _, extension = os.path.splitext(file_path)
    return extension.lower() in ['.xlsx', '.xls']

def excel_to_mysql(excel_file, db_config, table_name):
    df = pd.read_excel(excel_file)
    engine = create_engine(f"mysql+mysqlconnector://{db_config['user']}:{db_config['password']}@{db_config['host']}/{db_config['database']}")
    df.to_sql(name=table_name, con=engine, if_exists='replace', index=False)
path=sys.argv[1]
excel_file = r"{}".format(path) 
if is_excel_file(excel_file):
    db_config = {
        'user': 'root',
        'password': '220313',
        'host': 'localhost',
        'database': 'quiz'
    }
    table_name = 'quiz_data' 
    excel_to_mysql(excel_file, db_config, table_name)
    print("Success")
else:
    print(f"Error: The path '{excel_file}' is invalid.")
    sys.exit(1)
    
