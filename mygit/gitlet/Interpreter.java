package gitlet;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;

/** Interpreter class processes the commands.
 * @author Eurie Oh and Caroline Kim */
class Interpreter implements Serializable {
    /** The hashmap of all the commits made. */
    private HashMap<String, Commits> allCommits
                                    = new HashMap<String, Commits>();
    /** The arraylist of all the branches. */
    private ArrayList<Branch> branches = new ArrayList<Branch>();
    /** The current branch. */
    private Branch _current;
    /** modified stuff.*/
    private ArrayList<String> mod = new ArrayList<String>();
    /**Untracked stuff.*/
    private ArrayList<String> untracked = new ArrayList<String>();
    /** Checks for conflict. */
    private Boolean conflict;
    /** The constructor for this interpreter class. */
    public Interpreter() {
        String x = "doweneed";
    }
    /** If LINE is a recognized command other than a move, process it
     *  and return true. Otherwise, return false. */
    void init() {
        Init i = new Init("hey");
        Commits c = i.create();
        if (c != null) {
            allCommits.put(c.hash(), c);
            _current = new Branch("master", c.hash());
            branches.add(_current);
        }
    }
    /** Sets mod.*/
    public void setmod() {
        mod = new ArrayList<String>();
        Commits cur = allCommits.get(_current.head());
        ArrayList<Blob> curb = cur.bloblist();
        ArrayList<String> curn = cur.bnames();
        List<String> dir = Utils.plainFilenamesIn(".");
        List<String> staged = Utils.plainFilenamesIn(".gitlet/staged");
        List<String> removed = Utils.plainFilenamesIn(".gitlet/removed");
        for (String d : dir) {
            File df = new File(d);
            Blob dfb = new Blob(df, d);
            if (cur.contains(d) && !cur.contains(dfb)) {
                if (!staged.contains(d) && !removed.contains(d)) {
                    mod.add(d + " (modified)");
                }
            } else if (staged.contains(d)) {
                Blob dsb = new Blob(new File(".gitlet/staged/" + d), d);
                if (!dsb.equals(dfb)) {
                    mod.add(d + " (modified)");
                }
            }
        }
        for (String s : staged) {
            if (!dir.contains(s)) {
                mod.add(s + " (deleted)");
            }
        }
        for (String c : curn) {
            if (!removed.contains(c) && !dir.contains(c)) {
                mod.add(c + " (deleted)");
            }
        }
    }
    /** Set untracked.*/
    void setuntracked() {
        untracked = new ArrayList<String>();
        Commits cur = allCommits.get(_current.head());
        ArrayList<Blob> curb = cur.bloblist();
        ArrayList<String> curn = cur.bnames();
        List<String> dir = Utils.plainFilenamesIn(".");
        List<String> staged = Utils.plainFilenamesIn(".gitlet/staged");
        List<String> removed = Utils.plainFilenamesIn(".gitlet/removed");
        for (String d : dir) {
            if (!staged.contains(d) && !cur.contains(d)) {
                if (!removed.contains(d)) {
                    untracked.add(d);
                }
            }
            if (removed.contains(d)) {
                untracked.add(d);
            }
        }
    }
    /** Adding files command.
    * @param args takes in file name */
    void add(String[] args) {
        if (args[1] == null) {
            System.out.println("No file name.");
        } else {
            String name = args[1];
            File f = new File("./" + name);
            File copy = new File(".gitlet/staged/" + name);
            File r = new File(".gitlet/removed/" + name);
            if (r.exists()) {
                Utils.writeContents(f, Utils.readContents(r));
                r.delete();
                return;
            }
            Commits c = allCommits.get(_current.head());
            Blob b = c.get(name);
            if (b != null) {
                if (b.hash().equals(Utils.sha1(Utils.readContents(f)))) {
                    return;
                }
            }
            if (f.exists()) {
                if (copy.exists()) {
                    String chash = Utils.sha1(Utils.readContents(copy));
                    String fhash = Utils.sha1(Utils.readContents(f));
                    if (chash.equals(fhash)) {
                        return;
                    } else {
                        copy.delete();
                    }
                }
                Utils.writeContents(copy, Utils.readContents(f));
            } else {
                System.out.println("File does not exist.");
            }
        }
    }
    /** Prints out messages.
    * @param args takes in string array */
    void commitHelp(String[] args) {
        if (args.length < 2) {
            System.out.println("Please enter a commit message");
            return;
        }
        String message = args[1];
        for (int i = 2; i < args.length; i++) {
            message = message + " " + args[i];
        }
        if (message.equals("")) {
            System.out.println("Please enter a commit message");
            return;
        }
    }

    /** The commit command that makes the commits.
    * @param args takes in commit message */
    void commit(String[] args) {
        String message = args[1];
        commitHelp(args);
        List<String> staged
                        = Utils.plainFilenamesIn(new File(".gitlet/staged"));
        List<String> removed
                        = Utils.plainFilenamesIn(new File(".gitlet/removed"));
        if (staged.isEmpty() && removed.isEmpty()) {
            System.out.println("No changes added to the commit.");
        } else {
            Commits c = allCommits.get(_current.head());
            ArrayList<Blob> oldBlob = c.bloblist();
            ArrayList<Blob> newBlob = new ArrayList<Blob>();
            for (Blob b : oldBlob) {
                if (staged.contains(b.name())) {
                    File f = new File(".gitlet/staged/" + b.name());
                    newBlob.add(new Blob(f, b.name()));
                } else {
                    newBlob.add(b);
                }
            }
            for (String s : staged) {
                Boolean in = false;
                File x = new File(".gitlet/staged/" + s);
                Blob b = new Blob(x, s);
                for (Blob blob: newBlob) {
                    if (b.equals(blob)) {
                        in = true;
                    }
                }
                if (!in) {
                    newBlob.add(b);
                }
            }
            for (String s : removed) {
                Boolean in = false;
                Blob rm = null;
                File x = new File(".gitlet/removed/" + s);
                Blob b = new Blob(x, s);
                for (Blob blob: newBlob) {
                    if (b.equals(blob)) {
                        in = true;
                        rm = blob;
                    }
                }
                if (in && rm != null) {
                    newBlob.remove(rm);
                }
            }
            Commits t = new Commits(newBlob, c, message);
            if (t.equals(c)) {
                System.out.println("No changes added to the commit.");
                return;
            } else {
                allCommits.put(t.hash(), t);
                _current.change(t);
            }
            deleteall();
        }
    }
    /** Remove command that removes files.
    * @param args takes in the name of file that we want to remove */
    void rm(String[] args) {
        String name = args[1];
        Commits c = allCommits.get(_current.head());
        ArrayList<String> oldBlob = c.bnames();
        Boolean in = oldBlob.contains(name);
        File s = new File(".gitlet/staged/" + name);
        File f = new File(name);
        File r = new File(".gitlet/removed/" + name);
        if (s.exists()) {
            if (!c.contains(name)) {
                s.delete();
                return;
            }
            Utils.writeContents(r, Utils.readContents(s));
            s.delete();
            if (c.contains(name) && f.exists()) {
                f.delete();
            }
            return;
        }
        if (!r.exists()) {
            if (c.get(name) != null) {
                if (f.exists()) {
                    Utils.writeContents(r, Utils.readContents(f));
                    f.delete();
                } else {
                    Utils.writeContents(r, c.get(name).content());
                }
            } else {
                System.out.println("No reason to remove the file.");
            }
        }
    }
    /** Log command that gets the info about the commits. */
    void log() {
        Commits c = allCommits.get(_current.head());
        while (c.parent() != null) {
            System.out.println("===");
            System.out.println("Commit " + c.hash());
            System.out.println(c.time());
            System.out.println(c.message());
            System.out.println("");
            c = allCommits.get(c.parent());
        }
        System.out.println("===");
        System.out.println("Commit " + c.hash());
        System.out.println(c.time());
        System.out.println(c.message());
    }
    /** Command that displays info about all commits ever made. */
    void glog() {
        Collection<Commits> col = allCommits.values();
        Iterator<Commits> citer = col.iterator();
        while (citer.hasNext()) {
            Commits c = citer.next();
            System.out.println("===");
            System.out.println("Commit " + c.hash());
            System.out.println(c.time());
            System.out.println(c.message());
            System.out.println();
        }
    }
    /** Prints out the ids of all commits that have the commit message.
    * @param args takes in commit message to find */
    void find(String[] args) {
        if (args.length < 2) {
            System.out.println("Found no commit with that message.");
            return;
        }
        String message = args[1];
        for (int i = 2; i < args.length; i++) {
            message = message + " " + args[i];
        }
        Boolean none = true;
        Collection<Commits> col = allCommits.values();
        Iterator<Commits> citer = col.iterator();
        while (citer.hasNext()) {
            Commits c = citer.next();
            if (c.message().equals(message)) {
                none = false;
                System.out.println(c.hash());
            }
        }
        if (none) {
            System.out.println("Found no commit with that message.");
        }
    }
    /** Displays branches, staged files, and removed files. */
    void status() {
        setmod();
        setuntracked();
        System.out.println("=== Branches ===");
        for (Branch b : branches) {
            if (b == _current) {
                System.out.println("*" + b.name());
            } else {
                System.out.println(b.name());
            }
        }
        System.out.println("");
        System.out.println("=== Staged Files ===");
        List<String> staged
                    = Utils.plainFilenamesIn(new File(".gitlet/staged"));
        for (String s : staged) {
            System.out.println(s);
        }
        System.out.println("");
        System.out.println("=== Removed Files ===");
        List<String> removed
                    = Utils.plainFilenamesIn(new File(".gitlet/removed"));
        for (String r : removed) {
            System.out.println(r);
        }
        System.out.println("");
        System.out.println("=== Modifications Not Staged For Commit ===");
        for (String m : mod) {
            System.out.println(m);
        }
        System.out.println("");
        System.out.print("=== Untracked Files ===");
        for (String m : untracked) {
            System.out.println("");
            System.out.print(m);
        }
    }
    /** Checkout command with three different input possibilities.
    * @param args takes in what to checkout */
    void checkout(String[] args) {
        if (args.length < 2 || args.length > 4) {
            return;
        }
        if (args.length == 4) {
            if (!args[2].equals("--")) {
                System.out.println("Incorrect operands.");
                return;
            }
            checkoutHelp1(args);
        } else if (args.length == 2) {
            String name = args[1];
            Branch br = null;
            for (Branch b : branches) {
                if (b.name().equals(name)) {
                    br = b;
                }
            }
            checkoutHelp2(br);
        } else {
            if (!args[1].equals("--")) {
                System.out.println("Incorrect operands.");
                return;
            }
            checkoutHelp3(args);
        }
    }
    /** Gets the commit with this id.
     * @param id is the id u want check with
     * @return the commit if there is one*/
    Commits getCom(String id) {
        if (id.length() < 8) {
            return null;
        } else if (allCommits.get(id) != null) {
            return allCommits.get(id);
        } else {
            Collection<Commits> col = allCommits.values();
            Iterator<Commits> citer = col.iterator();
            while (citer.hasNext()) {
                Commits c = citer.next();
                if (c.hash().substring(0, 8).equals(id)) {
                    return c;
                }
            }
        }
        if (id.length() < 6) {
            return null;
        } else if (allCommits.get(id) != null) {
            return allCommits.get(id);
        } else {
            Collection<Commits> col = allCommits.values();
            Iterator<Commits> citer = col.iterator();
            while (citer.hasNext()) {
                Commits c = citer.next();
                if (c.hash().substring(0, 6).equals(id)) {
                    return c;
                }
            }
        }
        return null;
    }
    /** Helper function for commit id and file name form of checkout.
    * @param args takes in what to checkout for identification */
    void checkoutHelp1(String[] args) {
        String commitId = args[1];
        String fname = args[3];
        Commits c = getCom(commitId);
        if (c == null) {
            System.out.println("No commit with that id exists.");
            return;
        }
        ArrayList<Blob> blob = c.bloblist();
        Blob thisone = null;
        for (Blob b : blob) {
            if (b.name().equals(fname)) {
                thisone = b;
            }
        }
        if (thisone == null) {
            System.out.println("File does not exist in that commit.");
            return;
        } else {
            List<String> filenames = Utils.plainFilenamesIn(new File("."));
            if (filenames.contains(fname)) {
                File f = new File(fname);
                f.delete();
                File newFile = new File(fname);
                Utils.writeContents(newFile, thisone.content());
            } else {
                File newFile = new File(fname);
                Utils.writeContents(newFile, thisone.content());
            }
        }
    }
    /** Helper function for branch name form of checkout.
    * @param br takes in branch that we want to checkout */
    void checkoutHelp2(Branch br) {
        if (br == null) {
            System.out.println("No such branch exists.");
        } else {
            List<String> filenames = Utils.plainFilenamesIn(new File("."));
            File removed = new File(".gitlet/removed");
            List<String> rnames
                            = Utils.plainFilenamesIn(removed);
            File stagedFile = new File(".gitlet/staged");
            List<String> snames
                            = Utils.plainFilenamesIn(stagedFile);
            Commits t = allCommits.get(_current.head());
            ArrayList<Blob> tblob = t.bloblist();
            ArrayList<String> tblobnames = t.bnames();
            if (_current == br) {
                System.out.println("No need to checkout current branch.");
                return;
            }
            for (String f : filenames) {
                if (t.contains(f)) {
                    String tcont = t.get(f).hash();
                    byte[] temp = Utils.readContents(new File(f));
                    if (!Utils.sha1(temp).equals(tcont)) {
                        System.out.print("There is an untracked file in the ");
                        System.out.println("way; delete it or add it first.");
                        return;
                    }
                } else {
                    System.out.print("There is an untracked file in the way;");
                    System.out.println(" delete it or add it first.");
                    return;
                }
                if (rnames.contains(f) || snames.contains(f)) {
                    System.out.print("There is an untracked file in the way;");
                    System.out.println(" delete it or add it first.");
                    return;
                }
            }
            Commits c = allCommits.get(br.head());
            ArrayList<Blob> blob = c.bloblist();
            for (Blob b : blob) {
                if (filenames.contains(b.name())) {
                    overwrite(b);
                } else {
                    write(b);
                }
            }
            ArrayList<String> brnames = c.bnames();
            for (Blob b : tblob) {
                if (!brnames.contains(b.name())) {
                    File f = new File(b.name());
                    f.delete();
                }
            }
            deletedir(c);
            _current = br;
            deleteall();
        }
    }
    /** delete file in directory.
     * @param the commit to compare to*/
    void deletedir(Commits c) {
        File f = new File(".");
        List<String> dnames
                        = Utils.plainFilenamesIn(f);
        for (String x : dnames) {
            if (!c.contains(x)) {
                f.delete();
            }
        }
    }
    /** deletes all files in staged and removed.*/
    void deleteall() {
        File removed = new File(".gitlet/removed");
        List<String> rnames
                        = Utils.plainFilenamesIn(removed);
        File stagedFile = new File(".gitlet/staged");
        List<String> snames
                        = Utils.plainFilenamesIn(stagedFile);
        for (String f : snames) {
            File x = new File(".gitlet/staged/" + f);
            x.delete();
        }
        for (String f : rnames) {
            File x = new File(".gitlet/removed/" + f);
            x.delete();
        }
    }
    /** Overwrite the file with a new file.
    * @param b takes in blob that needs to be overwritten */
    void overwrite(Blob b) {
        File f = new File(b.name());
        f.delete();
        File newFile = new File(b.name());
        Utils.writeContents(newFile, b.content());
    }
    /** Simply writing the file without overwriting.
    * @param b takes in b that needs to be written */
    void write(Blob b) {
        File newFile = new File(b.name());
        Utils.writeContents(newFile, b.content());
    }
    /** Helper function for file name form of checkout.
    * @param args takes in what to checkout for identification */
    void checkoutHelp3(String[] args) {
        List<String> filenames = Utils.plainFilenamesIn(new File("."));
        Commits c = allCommits.get(_current.head());
        ArrayList<Blob> blob = c.bloblist();
        ArrayList<String> bname = c.bnames();
        String name = args[2];
        if (bname.contains(name)) {
            Blob thisone = null;
            for (Blob b : blob) {
                if (b.name().equals(name)) {
                    thisone = b;
                }
            }
            if (filenames.contains(name)) {
                File f = new File(name);
                f.delete();
                File newFile = new File(name);
                Utils.writeContents(newFile, thisone.content());
            } else {
                File newFile = new File(name);
                Utils.writeContents(newFile, thisone.content());
            }
        } else {
            System.out.println("File does not exist in that commit.");
        }
    }
    /** Creates a new branch with the given name.
    * @param args branch name */
    void branch(String[] args) {
        String bname = args[1];
        for (Branch br : branches) {
            if (br.name().equals(bname)) {
                System.out.println("A branch with that name already exists.");
                return;
            }
        }
        Branch b = new Branch(bname, _current.head());
        branches.add(b);
    }
    /** Deletes the branch with the given name.
    * @param args takes in the branch name to remove */
    void rmbranch(String[] args) {
        String bname = args[1];
        for (Branch br: branches) {
            if (br.name().equals(bname)) {
                if (_current == br) {
                    System.out.println("Cannot remove the current branch.");
                    return;
                }
                branches.remove(br);
                return;
            }
        }
        System.out.println("A branch with that name does not exist.");
    }
    /** Checks out all the files tracked by the given commit.
    * @param args takes in commit id to reset */
    void reset(String[] args) {
        String id = args[1];
        Commits c = getCom(id);
        if (c == null) {
            System.out.println("No commit with that id exits.");
            return;
        }
        ArrayList<String> names
                            = allCommits.get(_current.head()).bnames();
        List<String> filenames
                            = Utils.plainFilenamesIn(new File("."));
        List<String> staged
                        = Utils.plainFilenamesIn(new File(".gitlet/staged"));
        List<String> removed
                        = Utils.plainFilenamesIn(new File(".gitlet/removed"));
        for (String n : filenames) {
            int l = n.length();
            File i = new File(n);
            Blob j = new Blob(i, n);
            if (names.contains(n) && !names.contains(j)) {
                if (!staged.contains(n) && !removed.contains(n)) {
                    System.out.print("There is an untracked file in the way;");
                    System.out.println(" delete it or add it first.");
                    return;
                }
            }
        }
        ArrayList<Blob> oldBlob = c.bloblist();
        names = c.bnames();
        for (String n : filenames) {
            if (!c.contains(n) && !staged.contains(n)) {
                Utils.restrictedDelete(n);
            }
        }
        for (String n : names) {
            if (!filenames.contains(n)) {
                File f = new File(n);
                Utils.writeContents(f, c.get(n).content());
            }
        }
        deleteall();
        _current.change(c);
    }
    /** Helps with our merge.
    * @param args string array of the input
    * @return returns the branch */
    Branch mergeHelp(String[] args) {
        String bname = args[1];
        Branch b = null;
        for (Branch br : branches) {
            if (br.name().equals(bname)) {
                b = br;
            }
        }
        if (b == null) {
            System.out.println("A branch with that name does not exist.");
        }
        if (b == _current) {
            System.out.println("Cannot merge a branch with itself");
        }
        return b;
    }
    /** Merges files from the given branch into the current branch.
    * @param args takes in branch name to merge */
    void merge(String[] args) {
        if (args.length < 2) {
            return;
        }
        Branch b = mergeHelp(args);
        if (b == null) {
            return;
        }
        ArrayList<String> cnames = allCommits.get(_current.head()).bnames();
        List<String> filenames = Utils.plainFilenamesIn(new File("."));
        List<String> staged
                        = Utils.plainFilenamesIn(new File(".gitlet/staged"));
        List<String> removed
                        = Utils.plainFilenamesIn(new File(".gitlet/removed"));
        for (String n : filenames) {
            int l = n.length();
            if (!cnames.contains(n)
                    && (staged.contains(n) || removed.contains(n))) {
                System.out.println("You have uncommitted changes.");
                return;
            }
            if (!cnames.contains(n) && n.substring(l - 4, l).equals(".txt")) {
                System.out.print("There is an untracked file in the way;");
                System.out.println(" delete it or add it first.");
                return;
            }
        }
        Commits cur = allCommits.get(_current.head());
        Commits bcopy = allCommits.get(b.head());
        ArrayList<Commits> croutes = path(_current);
        ArrayList<Commits> broutes = path(b);
        Commits splitpoint = null;
        for (Commits c : croutes) {
            if (broutes.contains(c) && splitpoint == null) {
                splitpoint = c;
            }
        }
        if (splitpoint == null) {
            return;
        }
        if (splitpoint == allCommits.get(b.head())) {
            System.out.print("Given branch is an ancestor");
            System.out.println(" of the current branch.");
            return;
        } else if (splitpoint == cur) {
            _current.change(bcopy);
            System.out.print("Current branch fast-forwarded.");
            return;
        }
        ArrayList<Blob> bBlob = bcopy.bloblist();
        ArrayList<Blob> cBlob = cur.bloblist();
        ArrayList<Blob> sBlob = splitpoint.bloblist();
        ArrayList<Blob> nBlob = new ArrayList<Blob>();
        conflict = conflicts(nBlob, bcopy, splitpoint);
        justborc(nBlob, bcopy, splitpoint);
        justcors(nBlob, bcopy, splitpoint);
        justb(nBlob, bcopy, splitpoint);
        if (conflict) {
            return;
        }
        String m = "Merged " + _current.name() + " with " + b.name() + ".";
        Commits why = new Commits(nBlob, allCommits.get(_current.head()), m);
        _current.change(why);
        allCommits.put(why.hash(), why);
    }
    /** The case where s and cur do not contain the blob.
    * @param nBlob the arraylist of blobs
    * @param bcopy the commit to check
    * @param s the commit that we are looking at */
    public void justb(ArrayList<Blob> nBlob, Commits bcopy, Commits s) {
        ArrayList<Blob> bBlob = bcopy.bloblist();
        Commits cur = allCommits.get(_current.head());
        for (Blob b : bBlob) {
            if (!s.contains(b.name()) && !cur.contains(b.name())) {
                File newFile = new File(b.name());
                Utils.writeContents(newFile, b.content());
                if (conflict) {
                    File g = new File(".gitlet/staged/" + b.name());
                    Utils.writeContents(g, b.content());
                }
                nBlob.add(new Blob(newFile, b.name()));
            }
        }
    }
    /** The case where s and b does not contain
    * or s contains but b does not contain current blob.
    * @param nBlob the arraylist of blobs
    * @param b the commit to check
    * @param s the commit that we are looking at */
    public void justcors(ArrayList<Blob> nBlob, Commits b, Commits s) {
        ArrayList<Blob> cBlob = allCommits.get(_current.head()).bloblist();
        for (Blob c : cBlob) {
            if (!s.contains(c.name()) && !b.contains(c.name())) {
                nBlob.add(c);
            } else if (s.contains(c) && !b.contains(c.name())) {
                File g = new File(".gitlet/removed/" + c.name());
                if (g.exists()) {
                    g.delete();
                }
            }
        }
    }
    /** Gets the path of commits from b to initial.
    * @param b the branch that we want the path of
    * @return returns the arraylist of commits */
    public ArrayList<Commits> path(Branch b) {
        Commits c = allCommits.get(b.head());
        ArrayList<Commits> croutes = new ArrayList<Commits>();

        while (c != null) {
            croutes.add(c);
            if (c.parent() != null) {
                c = allCommits.get(c.parent());
            } else {
                c = null;
            }
        }
        return croutes;
    }
    /** Determines whether current and bcopy has conflicts
    * and adds blobs accordingly.
    * @param n the arraylist of blobs
    * @param bcopy the commit that we are looking at
    * @param splitpoint the commit of splitpoint
    * @return returns the boolean value of conflict */
    Boolean conflicts(ArrayList<Blob> n, Commits bcopy, Commits splitpoint) {
        Commits cur = allCommits.get(_current.head());
        ArrayList<Blob> bBlob = bcopy.bloblist();
        ArrayList<Blob> cBlob = cur.bloblist();
        Boolean conf = false;
        if (cBlob.isEmpty()) {
            return conf;
        }
        if (bBlob.isEmpty()) {
            return conf;
        }
        for (Blob c : cBlob) {
            if (bcopy.contains(c.name())
                            && !bcopy.contains(c) && !splitpoint.contains(c)) {
                conf = true;
                File bf = new File(".gitlet/1" + c.name());
                Utils.writeContents(bf, c.content());
                File cf = new File(c.name());
                Utils.writeContents(cf, bcopy.get(c.name()).content());
                File rf = writeconf(cf, bf);
                bf.delete();
                cf.delete();
                File r = new File(c.name());
                Utils.writeContents(r, Utils.readContents(rf));
                rf.delete();
                n.add(new Blob(r, c.name()));
            } else if (bcopy.contains(c)) {
                n.add(c);
            } else if (splitpoint.contains(c.name())) {
                if (!splitpoint.contains(c) && !bcopy.contains(c.name())) {
                    conf = true;
                    File cf = new File(c.name());
                    File rf = writeconf2(cf);
                    cf.delete();
                    File r = new File(c.name());
                    Utils.writeContents(r, Utils.readContents(rf));
                    rf.delete();
                    n.add(new Blob(r, c.name()));
                }
            }
        }
        if (conf) {
            System.out.println("Encountered a merge conflict.");
        }
        return conf;
    }
    /**writes the conflicted file.
    * @param f takes in file
    * @param g takes in another file
    * @return returns file **/
    File writeconf(File f, File g) {
        try {
            File r = new File(".gitlet/" + f.getName());
            ArrayList<String> a = new ArrayList<String>();
            FileWriter out = new FileWriter(r, false);
            BufferedReader x = new BufferedReader(new FileReader(g));
            String line1;
            String line2;
            a.add("<<<<<<< HEAD");
            while ((line1 = x.readLine()) != null) {
                a.add(line1);
            }
            x.close();
            a.add("=======");
            if (f != null) {
                BufferedReader b = new BufferedReader(new FileReader(f));
                while ((line2 = b.readLine()) != null) {
                    a.add(line2);
                }
                b.close();
            }
            a.add(">>>>>>>");
            for (String s : a) {
                out.write(s);
                out.write(System.lineSeparator());
            }
            out.close();
            return r;
        } catch (IOException e) {
            System.out.println(e);
            return null;
        }
    }
    /**writes the conflicted file for one file.
    * @param f takes in file
    * @return returns file **/
    File writeconf2(File f) {
        try {
            File r = new File(".gitlet/" + f.getName());
            ArrayList<String> a = new ArrayList<String>();
            FileWriter out = new FileWriter(r, false);
            BufferedReader x = new BufferedReader(new FileReader(f));
            String line1;
            String line2;
            a.add("<<<<<<< HEAD");
            while ((line1 = x.readLine()) != null) {
                a.add(line1);
            }
            x.close();
            a.add("=======");
            a.add(">>>>>>>");
            for (String s : a) {
                out.write(s);
                out.write(System.lineSeparator());
            }
            out.close();
            return r;
        } catch (IOException e) {
            System.out.println(e);
            return null;
        }
    }

    /** The case where bcopy doesn't contain blob content but contains the name,
    * bcopy does not contain the blob name, or current contains the blob name,
    * and bcopy contains the blob.
    * @param n the arraylist of blobs
    * @param bcopy the copy of the commit
    * @param split the split point */
    void justborc(ArrayList<Blob> n, Commits bcopy, Commits split) {
        Commits cur = allCommits.get(_current.head());
        ArrayList<Blob> bBlob = bcopy.bloblist();
        ArrayList<Blob> cBlob = cur.bloblist();
        ArrayList<Blob> sBlob = split.bloblist();
        for (Blob s : sBlob) {
            if (cur.contains(s)) {
                if (!bcopy.contains(s) && bcopy.contains(s.name())) {
                    File f = new File(s.name());
                    f.delete();
                    File newFile = new File(s.name());
                    Utils.writeContents(newFile,
                                        bcopy.get(s.name()).content());
                    n.add(new Blob(newFile, s.name()));
                } else if (!bcopy.contains(s.name())) {
                    File f = new File(s.name());
                    f.delete();
                    File g = new File(".gitlet/removed/" + s.name());
                    if (g.exists()) {
                        g.delete();
                    }
                }
            } else if (cur.contains(s.name()) && bcopy.contains(s)) {
                n.add(cur.get(s.name()));
            }
        }
    }
}
