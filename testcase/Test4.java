//recursion
abstract class Shape {
    abstract void draw(int depth);
}

class Circle extends Shape {
    void draw(int depth) {
        if (depth > 0) {
            System.out.println("Circle draw");
            draw(depth - 1);  // Recursive call
        }
    }
}

class Square extends Shape {
    void draw(int depth) {
        if (depth > 0) {
            System.out.println("Square draw");
            draw(depth - 1);  // Recursive call
        }
    }
}

public class Test4 {
    public static void main(String[] args) {
        Shape shape;
        Shape shape1=new Square();
        if (args.length > 0) {
            shape = new Circle();  // Creates a Circle if any argument is passed
        } else {
            shape = new Square();  // Otherwise, creates a Square
        }

        
        shape.draw(1);  // Polymorphic recursive call
        shape1.draw(1);
        System.out.println("huurrraaaayyyyy!!!!!!");
    }
}
