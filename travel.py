import sys

travels = [ [ 0 ] * 100 for i in range(100) ]
cities  = {}
line = raw_input( "city1 city2?" )
while line:
  cityn1, cityn2 = line.split()
  if cityn1 in cities:
    city1 = cities[cityn1]
  else:
    city1 = len(cities)
    cities[cityn1] = city1

  if cityn2 in cities:
    city2 = cities[cityn2]
  else:
    city2 = len(cities)
    cities[cityn2] = city2

  travels[city1][city2] += 1
  line = raw_input( "city1 city2 (enter if no more)?" )

startcity = []
for y in range( len(cities) ):
  if not any([travels[x][y] for x in range( len(cities) )]):
    startcity.append(y)

citynames = {x[1]: x[0] for x in cities.iteritems()}
if 1!=len(startcity):
  print "multiple start cities!"
  sys.exit()

startcity = startcity[0]
print "start city:", citynames[ startcity ]

def traverse( city ):
  printstr = "%s" % citynames[ city ]
  nextcities = travels[ city ]
  nextcitynos = [ i for i in range(len(cities)) if nextcities[i] > 0 ]

  maxprintstr = ""
  for nextcityno in nextcitynos:
    nextcities[ nextcityno ] -= 1
    newprintstr = traverse( nextcityno )
    nextcities[ nextcityno ] += 1
    if len(newprintstr) > len(maxprintstr):
      maxprintstr = newprintstr
  return printstr + ( " -> %s" % maxprintstr if maxprintstr else "" )

print traverse( startcity )

"""
SFO LAX 
LAX ATL 
ATL DEN 
DEN BOS 
BOS JFK 
JFK DEN 
DEN PHL 
PHL DFW 
DFW SEA 
SEA DEN 
DEN HOU 
HOU MIA
"""

