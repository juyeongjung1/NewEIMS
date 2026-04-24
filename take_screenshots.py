import os
from playwright.sync_api import sync_playwright

def take_screenshots():
    template_dir = r"c:\work\NewEIMS\NewEIMS\EIMS_SpringBoot_Basic\src\main\resources\templates"
    output_dir = r"c:\work\NewEIMS\NewEIMS\images"
    
    if not os.path.exists(output_dir):
        os.makedirs(output_dir)

    pages = [
        ("index.html", "UI1_top.png"),
        ("search.html", "UI2_search.png"),
        ("search_result.html", "UI3_search_result.png"),
        ("employee_detail.html", "UI_D_employee_detail.png"),
        ("input.html", "UI4_input.png"),
        ("input_confirm.html", "UI5_input_confirm.png"),
        ("input_complete.html", "UI6_input_complete.png"),
        ("change.html", "UI9_change.png"),
        ("change_confirm.html", "UI10_change_confirm.png"),
        ("change_complete.html", "UI11_change_complete.png"),
        ("delete_confirm.html", "UI7_delete_confirm.png"),
        ("delete_complete.html", "UI8_delete_complete.png")
    ]

    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        # Set a reasonable viewport for web app
        context = browser.new_context(viewport={"width": 1200, "height": 800})
        page = context.new_page()
        
        for html_file, out_file in pages:
            file_path = os.path.join(template_dir, html_file)
            if os.path.exists(file_path):
                print(f"Taking screenshot of {html_file}...")
                page.goto(f"file:///{file_path}")
                # Wait for any network resources like bootstrap
                page.wait_for_load_state("networkidle")
                page.screenshot(path=os.path.join(output_dir, out_file))
            else:
                print(f"File not found: {file_path}")
                
        browser.close()

if __name__ == "__main__":
    take_screenshots()
