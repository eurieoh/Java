package gitlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.FileNotFoundException;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Eurie Oh
 */
public class Main {
    /** Deserializes interpreter.
     * @returns that interpreter */
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
        return i;
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

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {
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
