from summa.summarizer import summarize
from summa.keywords import keywords
from pkgutil import get_data
import io
#from os.path import dirname, join

def main():
	bytes = get_data(__name__,"sample.txt")
	file = io.StringIO(bytes.decode())
	text = file.read().replace('\n','')
	print("Summary:")
	print(summarize(text, ratio = 0.2))
	print("")
	print("Key Words:")
	print(keywords(text))
