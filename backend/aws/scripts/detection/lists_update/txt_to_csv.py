import csv
import os

TXT_FILENAME = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'inputs', 'txt', 'SMS_Blacklist_OP31012026.txt')
CSV_FILENAME = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'inputs', 'csv', 'SMS_Blacklist_OP31012026.csv')
DESCRIPTION = 'OpenPhish'
PK = 'BLACKLIST_URL'         

def txt_to_csv():
    try:
        with open(TXT_FILENAME, 'r', encoding='utf-8') as txt_file, open(CSV_FILENAME, 'w', newline='', encoding='utf-8') as csv_file:
            writer = csv.writer(csv_file)
            writer.writerow(['PK', 'SK', 'DESCRIPTION'])
            
            for line in txt_file:
                writer.writerow([
                    PK,
                    line.strip(),
                    DESCRIPTION
                ])
    except FileNotFoundError:
        print(f"Error: File not found.")
    except Exception as e:
        print(f"An error occurred: {e}")

def main():
    txt_to_csv()
    print(f"Conversion completed.")

if __name__ == "__main__":
    main()