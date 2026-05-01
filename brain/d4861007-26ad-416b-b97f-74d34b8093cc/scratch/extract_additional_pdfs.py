import PyPDF2
import os

pdf_dir = r'C:\work\NewEIMS\追加機能の設計書\追加機能の設計書'
output_file = r'C:\work\NewEIMS\brain\d4861007-26ad-416b-b97f-74d34b8093cc\scratch\additional_functions_text.txt'

# Ensure the scratch directory exists (although the system should handle it)
os.makedirs(os.path.dirname(output_file), exist_ok=True)

with open(output_file, 'w', encoding='utf-8') as out:
    for filename in sorted(os.listdir(pdf_dir)):
        if filename.endswith('.pdf'):
            out.write(f'=== File: {filename} ===\n')
            pdf_path = os.path.join(pdf_dir, filename)
            try:
                with open(pdf_path, 'rb') as file:
                    reader = PyPDF2.PdfReader(file)
                    for page_num in range(len(reader.pages)):
                        page_text = reader.pages[page_num].extract_text()
                        if page_text:
                            out.write(f'--- Page {page_num + 1} ---\n')
                            out.write(page_text + '\n')
            except Exception as e:
                out.write(f'Error reading {filename}: {e}\n')
            out.write('\n\n')

print(f"Extracted text to {output_file}")
