import prettytable as pt
from sys import argv
table = pt.PrettyTable()
table.field_names = argv[1].split(',')
s=argv[2].split('|')
if s[-1]=='':s=s[:-1]
for i in s:
    table.add_row(i.split(','))
print(table)


