#Exercícios Futebol Tabel Rank By Paulo
import sys
import time as t

def menu():
    var = True
    while(var):
        print("""==================== Menu ====================
            [0] - Sair
            [1] - Ler dados Rank
            [2] - Lista de Clubes de país
            [3] - Guardar Lista de clubes
            [4] - Informações de clube
            [5] - prints
==============================================""")

        inp = input("Opção: ")
        print()
        print()
        match inp:
            case "0":
                print("A sair do programa...")
                t.sleep(1)
                var = False
            case "1":
                fname = "Soccer_Football Clubs Ranking.csv"
                lst_rank = futfile(fname)
                print("Lista criada")
                var = True
            case "2":
                country = input('Country: ')
                lst_rank_clubs = countryClubs(country, lst_rank)
                var = True
            case "3":
                if(country == None or len(lst_rank_clubs) == 0):
                    print("Impossível!")
                    return True
                writeclubs('country_clubs.csv', country, lst_rank_clubs)
                print("Lista guardada no ficheiro "'country_clubs.csv'"")
                var = True
            case "4":
                if(len(lst_rank_clubs) == 0):
                    print("Impossível!")
                    var = True
                clubn = input('Club: ')
                print(infoClub(lst_rank_clubs, clubn))
                var = True
            case "5":
                vari = True
                while(vari):
                    print("Print what? ")
                    print("| 0 - Return | 1 - Rank Clubs | 2 - Rank countries Ordered | 3- Rank countries |")
                    r = input("R:")
                    match r:
                        case "0":
                            vari = False
                            var = True
                        case "1":
                            print(lst_rank)
                            vari = True
                            var = True
                        case "2":
                            print(rankordered(lst_rank_clubs))
                            vari = True
                            var = True
                        case "3":
                            print(rankMByC(lst_rank_clubs))
                            vari = True
                            var = True


def futfile(fname):
    lst_rank_clubs = []
    with open(fname, 'r', encoding='utf-8') as fl:
        for line in fl:
            if line.startswith('ranking,club name ,'):
                continue
            line = line.strip()
            line = line.split(',')
            if line[-1] == '-':
                line[4] = -int(line[4])
            tup = (line[0], line[1], line[2], int(line[3]), int(line[4]), int(line[5]), line[6])
            lst_rank_clubs.append(tup)
    return lst_rank_clubs


#Posição do elemento no tuplo
rank, club, countryp, points, positionchange, ppoints, change = 0, 1, 2, 3, 4, 5, 6

def countryClubs(country, lst_rank_clubs, file=sys.stdout):
    print(f'Clubs of {country}', file=file)
    print('=' * 65, file=file)
    print(f'| {"Club":^30s} | {"Rank":^10s} | {"Score Points":^15s} |', file=file)
    print('=' * 65, file=file)
    for cl in lst_rank_clubs:
        if cl[countryp] == country:
            print(f'| {cl[club]:^30s} | {cl[rank]:^10} | {cl[points]:^15} |', file=file)
            print('-' * 65, file=file)
    return lst_rank_clubs


def writeclubs(filw, country, lst_rank_clubs, file=sys.stdout):
    with open(filw, 'w', encoding='utf-8')as fl:
        countryClubs(country, lst_rank_clubs, fl)


def clubsByCountry(lst_rank_clubs):
    dic_C = {}
    for el in lst_rank_clubs:
        if el[countryp] not in dic_C.keys():
            dic_C[el[countryp]] = [(el[rank], el[club])]
        else:
            dic_C[el[countryp]].append((el[rank], el[club]))
    return dic_C


def raiseclub(lst_rank_clubs):
    eq = max(lst_rank_clubs, key= lambda t: t[positionchange])
    return eq[club]


def infoClub(lst_rank_clubs, cl):
    for el in lst_rank_clubs:
        if cl == el[club]:
            return el
        else:
            continue


def rankMByC(lst_rank_clubs):
    dic_clubsbyc = clubsByCountry(lst_rank_clubs)
    dic_desord = {}
    for county, clubs in dic_clubsbyc.items():
        dic_desord[county] = round(sum(int(c[0]) for c in clubs) / len(clubs),2)
    return dic_desord

def rankordered(lst_rank_clubs):
    dic_rank_desord = rankMByC(lst_rank_clubs)
    dic_ordered = {county:rank for county, rank in sorted(dic_rank_desord.items(), key= lambda r: dic_rank_desord[r[0]])}
    for county, rankm in dic_ordered.items():
        print(f'{county} ==> {rankm}')


def main():
    menu()

if __name__ == main():
    main()