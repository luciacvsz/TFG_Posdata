import os
import pandas as pd

RAW_FILENAME = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'inputs', 'csv', 'SMS_Whitelist_TrancoRaw31012026.csv')
CLEAN_FILENAME = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'inputs', 'csv', 'SMS_Whitelist_TrancoClean31012026.csv')
DESCRIPTION = 'Tranco'
PK = 'WHITELIST_URL'

def parse_tranco_list():
    df = pd.read_csv(RAW_FILENAME, header=None, names=['Rank', 'Domain'])
    df_formatted = pd.DataFrame()
    df_formatted['SK'] = df['Domain'].str.strip()
    df_formatted['PK'] = PK
    df_formatted['DESCRIPTION'] = DESCRIPTION
    df_formatted.to_csv(CLEAN_FILENAME, index=False, encoding='utf-8')

def main():
    parse_tranco_list()
    print("Tranco list parsing completed.")

if __name__ == "__main__":
    main()