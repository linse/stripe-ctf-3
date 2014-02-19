import string
import hashlib
import os
import sys
import random
from itertools import product

clone_spec = "lvl1-qyjqltug@stripe-ctf.com:level1"
public_username = "user-vyz65jfl"
username = "linse"
difficulty = os.popen("cat difficulty.txt").read()

def prepare_index():
     os.system("perl -i -pe 's/($ENV{public_username}: )(\d+)/$1 . ($2+1)/e' LEDGER.txt")
     cmd = 'grep -q "'+public_username+'" LEDGER.txt || echo "'+public_username+': 1" >> LEDGER.txt'
     os.popen(cmd).read()
     os.popen("git add LEDGER.txt").read() 

def commithash(body):
    obj = "commit %d\0%s" % (len(body)+32, body)
    return hashlib.sha1(obj.encode('ascii'))

def solve():
    # Brute force until you find something that's lexicographically
    # small than $difficulty.

    # Create a Git tree object reflecting our current working
    # directory
    tree = os.popen("git write-tree").read()
    parent = os.popen("git rev-parse HEAD").read()
    timestamp = os.popen("date +%s").read()
    body="tree "+tree+"\n"+ "parent "+parent+"\n"+ "author CTF user <me@linse.me> "+timestamp+" +0000"+"\n"+ "committer CTF user <me@linse.me> "+timestamp+" +0000"+"\n"+ "\n"+ "Give me a Gitcoin\n"+ "\n"#+ counter

    # python hashlib incremental
    sha1 = hashlib.sha1("commit %u\0%s" % (len(body+"\n")+32,body))
    counter=0
    while True: 
         counter = '%32X' % random.getrandbits(128)
         #sys.stdout.write('.')

         # traditional: git
         #cmd='git hash-object -t commit --stdin <<< "'+body+counter+'"'
         #s1=os.popen(cmd).read()

         s = sha1.copy()
         s.update(counter+"\n")
         
         if s.hexdigest() <= difficulty: 
            print ""
            print "Mined a Gitcoin with commit: "+s.hexdigest()
            os.popen('git hash-object -t commit --stdin -w <<< "'+body+counter+'"  > /dev/null').read()
            os.popen('git reset --hard "'+s.hexdigest()+'" > /dev/null')
            break

def reset():
    print("We would fetch and reset now if the server was still up.")
    #os.popen("git fetch origin master >/dev/null 2>/dev/null").read()
    #os.popen("git reset --hard origin/master >/dev/null").read()

# Set up repo
#local_path="./"+clone_spec
local_path="./level1"

if os.path.exists(local_path): 
    cmd='echo "Using existing repository at '+local_path+'"'
    value = os.popen(cmd).read()
    cmd='cd "'+local_path+'"'
    value = os.popen(cmd).read()
else:
    cmd='echo "Cloning repository to '+local_path+'"'
    value = os.popen(cmd).read()
    cmd='git clone '+clone_spec+' '+local_path
    value = os.popen(cmd).read()
    cmd='cd '+local_path
    value = os.popen(cmd).read()

while True:
    prepare_index()
    solve()
    print("We would push now if the server was still up")
    #if os.popen("git push origin master").read(): 
    #    print "Success :)"
    #    break
    #else:
    #    print "Starting over :("
    #    reset()
