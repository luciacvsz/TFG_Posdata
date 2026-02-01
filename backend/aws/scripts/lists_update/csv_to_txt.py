import csv
import os

# REVISAR SI HAY QUE PONER COSAS A LIMPIO DE AQUÍ

TXT_FILENAME = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'inputs', 'txt', 'SMS_Whitelist_TrancoCleanReduced31012026.txt')
CSV_FILENAME = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'inputs', 'csv', 'SMS_Whitelist_TrancoCleanReduced31012026.csv')

def csv_to_txt():
    try:
        with open(CSV_FILENAME, 'r', encoding='utf-8') as csv_file, open(TXT_FILENAME, 'w', encoding='utf-8') as txt_file:
            reader = csv.reader(csv_file)

            header = next(reader)

            for row in reader:
                domain = row[0].strip()
                txt_file.write(f"{domain}\n")            
            
    except FileNotFoundError:
        print(f"Error: File not found.")
    except Exception as e:
        print(f"An error occurred: {e}")

def main():
    csv_to_txt()
    print(f"Conversion completed.")

if __name__ == "__main__":
    main()