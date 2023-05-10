Exemple: Liftul e la etajul 0 si are 2 request-uri la 5 si la 9. Intre timp apare un call la etajul 4 care cere un request la etajul 8.
Solutie: Ia-l pe ala de la etajul 4, du-l pe ala la 5, du-l la 8 si dupa la 9. Cand vrei sa duci pe cineva, trebuie sa faci minimul
din etajele care sunt pe directia liftului de mers, de asta il duce la 8 si dupa la 9 si nu invers. Atunci cand nu mai sunt request-uri la etaje mai inalte, liftul isi schimba directia de mers si ii duce pe eventualii clienti la minimul dintre diferenta  
 etajului liftului si numerele mai mari (daca merge liftul in sus) sau numerele mai mici (daca merge liftul in jos). Practic, atunci cand liftul merge in sus la request, selecteaza minimul din numerele mai mari decat etajul actual al liftului si atunci cand liftul merge in jos la request, selecteaza maximul din numerele mai mici decat etajul actual al liftului. Faci asta pana cand nu mai sunt etaje mai mari sau mai mici la request-uri decat etajul lift-ului.

Elevator states:
Up, Down, Idle (Going Up), Idle (Going Down)

Idle inseamna ca ori asteapta clienti in called pentru ca nu mai sunt alte cerinte noi, ori urca oamenii in lift, perioada de urcare va fi de 4-5 secunde.

Etajul 0 nu are buton 'Down'. Etajul n-1 (n = nr. etaje) nu are buton 'Up'.

## ATENTIE

- Vezi o structura de date care se comporta ca o lista, gen are .get() etc si este eficienta pentru inserari si remove-uri, avand in vedere ca de obicei, asta o sa faci.
- Probabil o sa folosesti JDBC cu PostgreSQL. Vezi cum poti sa faci functii si proceduri in postgreSQL si sa le apelezi in java ca sa iti iei datele din baza de date. O sa fie un singur tabel, cel cu lifturi si optional unul cu thread-uri in care sa spui de unde a apelat liftul si unde a vrut sa se duca liftul. O sa faci procedura sau functie ca sa iti ia toate datele despre lifturi si apoi folosesti datele astea ca sa iti creezi clasa Elevator cu diferitele ei caracteristici, si o procedura sau functie (PL/SQL cum ar veni) ca sa iti introduci acele loguri (sau datele pentru al 2-lea tabel numit Clients) care te poate ajuta pentru statistici. Poti arunca exceptii daca nu ai lifturi in baza de date, etc.

Fiecare lift va avea thread-ul lui, fiecare thread va avea acele 2 functii, nextCalled() si nextRequest() care vor fi apelate dupa ce thread-ul isi da fetch la called si request-ului lui la variabilele @@ statice volatile @@, static ca sa fie accesate de fiecare thread si volatile ca sa fie luate mereu din memorie.

## Tables

- "Trips" table: This table could store information about elevator trips, such as the starting and ending floor of the trip, the timestamp of the trip, and the number of passengers on the trip. You could then write a function that takes an elevator ID as input and returns the list of trips that the elevator has completed.
  Columns: ElevatorID, startedAt, stoppedAt, nrOfStops (numarul de opriri), direction varchar2 (up/down), weightTransported (in KG)

- Elevators table:
  ElevatorID, maxCapacity (verificare in plus! daca capacitatea este maxima <mark>FIECARE CLIENT ARE GREUTATE IN KG</mark>, serveste doar request-urile pentru ca daca ai servi apelurile in called, oricum nu ai mai avea loc sa ii iei. Vei adauga o valoare booleana, isAtMaxCapacity si daca da, serveste doar request-urile, daca nu, ia din amandoua), haveMirror, backgroundColor

## Implementare

Se va folosi ArrayList.
Server-ul va avea un List<List<Client>> calls. Acesta va contine chemarile lifturilor pentru toate lifturile. Lift-ul cu id 0 -> calls.get(0). Aceste calls vor fi in ordine crescatoare si fiecare lift le va prelua o data la cateva secunde.
Fiecare lift, pe langa lista de calls cu clienti va avea o lista de request-uri.

<mark>Ca sa fie verificarea mai eficienta, poti verifica daca lista cu calls si-a schimbat size-ul fata de ultima data, daca nu, nu are rost sa mai verifici cu nextCall() si nextRequest(). Daca e la capacitate maxima - 100kg , nici nu mai da fetch din server la calls, nu are rost.

Client:
int from, to;
long weight; (in kg);

## Pasi

1. Seteaza-ti JDBC, vezi cum faci proceduri, apeleaza-le si vezi ca poti lua date din baza de date.
2. Creeaza clasele Elevator pentru fiecare rand din tabela.
3. ....


Ideas for SGBD:
1. O functie care ordoneaza dupa un parametru dat dinamic + ascendent sau descendent + table_name
ex: create or replace procedure ordoneaza (table_name text, camp text, order text)
query := 'select * from {table_name} order by {camp} {order}'

2. O functie cu 3 parametrici dinamici, table_name, camp si valoare care returneaza un result set si imita 'WHERE'