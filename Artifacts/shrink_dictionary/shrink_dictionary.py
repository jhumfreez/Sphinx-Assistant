import os


file_name = 'small_cmudict-en-us.dict'

with open(file_name, 'w+') as f:

    with open('common_names_words.txt', 'r') as n:
        common = []
        for line in n:
            line = line[:-1]
            common.append(line.strip())
    with open('cmudict-en-us.dict', 'r') as d:
        for line2 in d:
            # account for e.g, "abend" and "abend(2)"
            word = line2.split()[0].split('(')[0].strip()
            if word in common:
                f.write(line2)
