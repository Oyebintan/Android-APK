import urllib.request
with urllib.request.urlopen('https://raw.githubusercontent.com/Oyebintan/Final-Year-Project/main/index.html') as response:
    html = response.read()
    print(html.decode('utf-8'))
