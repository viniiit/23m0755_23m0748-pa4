//fields changing in called method
class BaseClass {
    public void foo() {
        System.out.println("BaseClass foo");
    }
}

class DerivedClass1 extends BaseClass {
    BaseClass f;
    public void foo() {
        DerivedClass2 c=new DerivedClass2();
        this.f=c;
        System.out.println("DerivedClass1 foo");
    }
}

class DerivedClass2 extends BaseClass {
    public void foo() {
        System.out.println("DerivedClass2 foo");
    }
}

class DerivedClass3 extends BaseClass {
    public void foo() {
        System.out.println("DerivedClass3 foo");
    }
}

class DerivedClass4 extends BaseClass {
    public void foo() {
        System.out.println("DerivedClass4 foo");
    }
}

class DerivedClass5 extends BaseClass {
    public void foo() {
        System.out.println("DerivedClass5 foo");
    }
}

public class Test3 {
    public static void main(String[] args) {
    
        BaseClass a=new BaseClass();
        DerivedClass1 b=new DerivedClass1();
        DerivedClass1 c=new DerivedClass1();
        b.f=a;
        b.foo();
        c.foo();
        b.f.foo();
        c.f.foo();

    }
}
