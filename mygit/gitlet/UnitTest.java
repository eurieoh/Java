package gitlet;

import ucb.junit.textui;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;



/** The suite of all JUnit tests for the gitlet package.
 *  @author Caroline Kim & Eurie Oh
 */
public class UnitTest {

    /** Run the JUnit tests in the loa package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    public static void main(String[] ignored) {
        textui.runClasses(UnitTest.class);
    }

    @Test
    public void fileTest() {
        mainmethod(new String[] {"init"});
        File dog = new File("dog.txt");
        String s = "hello its me";
        byte[] b = s.getBytes();
        Utils.writeContents(dog, b);
        File cat = new File("cat.txt");
        s = "i've been wondering";
        b = s.getBytes();
        Utils.writeContents(cat, b);
        File bird = new File("bird.txt");
        s = "when you'll do the dishes";
        b = s.getBytes();
        Utils.writeContents(bird, b);
        File f = new File(".gitlet");
        System.out.println(f.isDirectory());

        String[] add = new String[] {"add", "dog.txt"};
        mainmethod(add);

        String[] first = new String[] {"commit", "first commit"};
        mainmethod(first);

        String[] remove = new String[] {"rm", "dog.txt"};
        mainmethod(remove);

        String[] second = new String[] {"commit", "second commit"};
        mainmethod(second);

        String[] third = new String[] {"commit", "no commits made"};
        mainmethod(third);
    }

    public static Interpreter deser() {
        Interpreter i = null;
        try {
            File f = new File(".gitlet/dummy/interpreter.txt");
            if (f.exists()) {
                FileInputStream input = new FileInputStream(f);
                ObjectInputStream in = new ObjectInputStream(input);
                i = (Interpreter) in.readObject();
                in.close();
            } else {
                i = new Interpreter();
            }
        } catch (IOException e) {
            System.out.println(e);
            return null;
        } catch (ClassNotFoundException e) {
            System.out.println(e);
            return null;
        }
        return null;
    }
    /** Serializes interpreter.
     * @param i is the interpreter*/
    public static void ser(Interpreter i) {
        try {
            File file = new File(".gitlet/dummy/interpreter.txt");
            FileOutputStream output = new FileOutputStream(file);
            ObjectOutputStream out = new ObjectOutputStream(output);
            out.writeObject(i);
            out.close();
        } catch (FileNotFoundException e) {
            System.out.println(e);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public static void mainmethod(String... args) {
        Interpreter i = deser();
        if (args == null) {
            System.out.println("Input command.");
            return;
        }
        if (args[0] != null) {
            switch (args[0]) {
            case "init":
                i.init();
                break;
            case "add":
                i.add(args);
                break;
            case "commit":
                i.commit(args);
                break;
            case "rm":
                i.rm(args);
                break;
            case "log":
                i.log();
                break;
            case "global-log":
                i.glog();
                break;
            case "find":
                i.find(args);
                break;
            case "status":
                i.status();
                break;
            case "checkout":
                i.checkout(args);
                break;
            case "branch":
                i.branch(args);
                break;
            case "rm-branch":
                i.rmbranch(args);
                break;
            case "reset":
                i.reset(args);
                break;
            case "merge":
                i.merge(args);
                break;
            default:
                System.out.println("Not an existing command.");
            }
        }
        ser(i);
    }
}
