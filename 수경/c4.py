import requests
from lxml.html import parse
from io import StringIO
import os, sys
from PIL import Image
from bs4 import BeautifulSoup
import urllib.request
import random

while(1):
    # 검색할 이미지의 키워드 입력
    keyword = input("검색할 이미지를 입력하세요 : ")
    if keyword == -1:
        break
    url = 'https://www.google.co.kr/search?q='+keyword+'&source=lnms&tbm=isch&sa=X&ved=0ahUKEwic-taB9IXVAhWDHpQKHXOjC14Q_AUIBigB&biw=1842&bih=990'

    # html 소스 가져오기
    text = requests.get(url, headers={'user-agent': ':Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36'}).text

    # html 문서로 파싱
    text_source = StringIO(text)
    parsed = parse(text_source)

    # root node 
    doc = parsed.getroot()

    # img 경로는 img 태그안에 src에 있음
    imgs = doc.findall('.//img')

    img_list = []   # 이미지 경로가 담길 list
    for a in imgs:
        img_list.append(a.get('data-src'))
        print(a.get('data-src'))
        name = random.randrange(1,1001)  #저장할 이미지 이름 랜덤숫자 붙여서 생성
        full_name = "img/c4" + str(name) + ".jpg"
        try:
            urllib.request.urlretrieve(str(a.get('data-src')),full_name)  #url이용해서 img폴더에 저장
        except ValueError:
            print("None!")
