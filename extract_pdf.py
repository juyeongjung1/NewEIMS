import PyPDF2

pdf_path = r'C:\work\NewEIMS\NewEIMS\参考ファイル\Spring Boot メインテキスト.pdf'
try:
    with open(pdf_path, 'rb') as file:
        reader = PyPDF2.PdfReader(file)
        text = ''
        for page_num in range(len(reader.pages)):
            page_text = reader.pages[page_num].extract_text()
            if page_text:
                text += page_text + '\n'
        
        # We need to find Chapter 7. 
        import re
        # Look for class="something" in the text
        classes = re.findall(r'class="([^"]+)"', text)
        classes_split = []
        for c in classes:
            classes_split.extend(c.split())
        
        # Also look for references like "container", "btn", etc.
        print("Found class attributes in the PDF:")
        print(set(classes_split))
        
        # Write the whole text to a file so we can grep it if needed
        with open('pdf_text.txt', 'w', encoding='utf-8') as out:
            out.write(text)
            
except Exception as e:
    print(e)
