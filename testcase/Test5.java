//Missing method in hierarchy
class BaseClass {
    public void foo() {
        System.out.println("BaseClass foo");
    }
}

class DerivedClass1 extends BaseClass {
    public void foo() {
        System.out.println("DerivedClass1 foo");
    }
}

class DerivedClass2 extends DerivedClass1 {
    public void foo() {
        System.out.println("DerivedClass2 foo");
    }
}

class DerivedClass3 extends DerivedClass2 {
    public void foo() {
        System.out.println("DerivedClass3 foo");
    }
}

class DerivedClass4 extends DerivedClass3 {
    
}

class DerivedClass5 extends DerivedClass4 {
    
}

public class Test5 {
    public static void main(String[] args) {
        BaseClass obj;
        DerivedClass1 d1=new DerivedClass1();
        BaseClass obj2=new DerivedClass2();
        BaseClass obj5=new DerivedClass5();
        if (args.length > 0) {
            obj = new DerivedClass1();
        } else {
            obj = new DerivedClass2();
        }
        
        obj.foo();  // Dynamic dispatch based on obj's actual type
        d1.foo();
        obj2.foo();
        obj5.foo();
    

    }
}
