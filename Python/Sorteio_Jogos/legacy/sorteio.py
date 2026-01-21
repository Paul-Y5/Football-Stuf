import random as r
import time as t

#Funções
def readfile(name):
    data_clubs = {}
    name += ".txt"
    with open(name, "r", encoding="utf-8") as list:
        for line in list:
            if line.startswith("Equipas") or line.startswith("Rank") or len(line.strip()) == 0:
                continue
            line = line.strip()
            line = line.split("|")
            club = (line[0], line[1], line[2], line[3])
            if(club in data_clubs.values()):
                print("Clube já adicionado!")
                continue
            data_clubs[scanComp(line[-1]) + "[" + str(line[0]) + "]"] = club
    return data_clubs


def scanComp(string):
    string = string.split(" ")
    if(string[-1] == "Campeões"):
        return "LC"
    if(string[1] == "Conferências"):
        return "LCE"
    if(string[-1] == "Europa"):
        return "LE"
    return "Unknown European Competition"


def organize(data):
    games = {"casa": [], "fora": []}
    lc = []
    le = []
    lce = []
    for k in data.keys():
        club = (data[k][0], data[k][1], data[k][2], data[k][3], games)
        if(k.startswith("LCE")):
            lce.append(club)
            continue
        elif(k.startswith("LC")):
            lc.append(club)
            continue
        elif(k.startswith("LE")):
            le.append(club)
            continue
        else:
            print("Error ao adicionar equipa!")
    return lc, le, lce


def sortingShuffles(lst):
    cf = ["casa", "fora"]
    list_apoio = lst.copy()
    
    if len(lst) % 2 != 0 or len(lst) < 2:
        print("Número de equipas impossível!")
        return
    
    while True:
        if(len(list_apoio) == 0):
            return lst
        club = r.choice(lst)

        if(len(club[4]["casa"]) == 4 and len(club[4]["fora"]) == 4):
            list_apoio.remove(club)

        local = r.choice(cf)
        adversario = r.choice(list_apoio)
        
        other = "fora" if local == "casa" else "casa"

        if(adversario[4][other].count(club[1]) == 0 and club[2] != adversario[2]):
            club[4][local].append(adversario[1])
            adversario[4][other].append(club[1])
            print(club[1], "vs", adversario[1])
        else:
            r.shuffle(list_apoio)


def gameSorting(listC, listCE, listE):
    #Sorteio
    #Sorteio para a Liga dos Campeões
    print("Sorteio para a Liga dos Campeões")
    r.shuffle(listC)
    sortingShuffles(listC)
    print()
    #Sorteio para a Liga Europa
    print("Sorteio para a Liga Europa")
    r.shuffle(listE)
    sortingShuffles(listE)
    print()
    #Sorteio para a Liga Conferências Europa
    r.shuffle(listCE)
    sortingShuffles(listCE)
    print()


def menu():
    clubs_list = readfile("EE")
    listC, listE, listCE = organize(clubs_list)
    gameSorting(listC, listCE, listE)
    print(listC)


def main():
    print("Loading program", end="")
    for loop in range(3):
        print(" ...", end="")
        t.sleep(1)
    print()
    menu()


if __name__ == "__main__":
    main()
