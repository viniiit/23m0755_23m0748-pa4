import java.util.*;
import soot.*;
import java.util.stream.Collectors;
import soot.options.Options;
import soot.jimple.*;


public class AnalysisTransformer extends BodyTransformer {

    public AnalysisTransformer() {
        super();
        setupSpark();
    }

    private void setupSpark() {
        // Set options for Spark if not already set
        Options.v().setPhaseOption("cg.spark", "enabled:true");
        Options.v().setPhaseOption("cg.spark", "on-fly-cg:true");
        Options.v().setPhaseOption("cg.spark", "verbose:true");
    }
    // public void printVariablePointsToInfo() {
    //     System.out.println("Variable Points-To Information:");
    //     // Create a copy of the keys to avoid ConcurrentModificationException
    //     for (String key : new HashSet<>(variableToPointedTypes.keySet())) {
    //         Set<String> types = variableToPointedTypes.get(key);
    //         if (types != null) {
    //             System.out.println("Variable " + key + " points to types: " + types);
    //         }
    //     }
    // }

    private boolean methodExists(SootClass sootClass, String methodName, List<Type> paramTypes) {
        for (SootMethod method : sootClass.getMethods()) {
            if (method.getName().equals(methodName) && method.getParameterTypes().equals(paramTypes)) {
                return true; // Method with the same signature already exists
            }
        }
        return false; // No method with the same signature found
    }
    
    private Map<String, Set<String>> variableToPointedTypes = new HashMap<>();

    @Override
    protected void internalTransform(Body body, String phaseName, Map<String, String> options) {
        PatchingChain<Unit> units = body.getUnits();
        

        
        setupSpark();

        // Access the points-to analysis from Scene
        PointsToAnalysis pta = Scene.v().getPointsToAnalysis();

        for (Unit unit : body.getUnits()) {
            for (ValueBox box : unit.getUseAndDefBoxes()) {
                Value value = box.getValue();
                if (value instanceof Local) { // Check if the value is a local variable
                    Local local = (Local) value;
                    PointsToSet pts = pta.reachingObjects(local);
                    if (!pts.isEmpty()) {
                        Set<String> types = new HashSet<>();
                        Iterator<Type> typeIt = pts.possibleTypes().iterator();
                        while (typeIt.hasNext()) {
                            Type t = typeIt.next();
                            types.add(t.toString());
                        }
                        variableToPointedTypes.put(local.getName(), types);
                    }
                }
            }
        }

        // printVariablePointsToInfo();

        PatchingChain<Unit> units1 = body.getUnits();
        Iterator<Unit> unitIt = units1.snapshotIterator();
        Map<Unit, Unit> oldToNewUnitMap = new HashMap<>();
        while (unitIt.hasNext()) {
            Unit u = unitIt.next();
            
            if (u instanceof IfStmt) {
                IfStmt ifStmt = (IfStmt) u;
                
            } else if (u instanceof GotoStmt) {
                GotoStmt gotoStmt = (GotoStmt) u;
                
            }
            if (u instanceof Stmt) {
                Stmt stmt = (Stmt) u;
                if (stmt.containsInvokeExpr()) {
                    InvokeExpr invokeExpr = stmt.getInvokeExpr();
                    if (invokeExpr instanceof VirtualInvokeExpr) {
                        SootMethod method = invokeExpr.getMethod();
                        // Check if the method belongs to a class in the Java standard library or another library
                        if (method.getDeclaringClass().getPackageName().startsWith("java") || 
                            method.getDeclaringClass().getPackageName().startsWith("javax")) {
                            continue;  // Skip this invocation
                        }

                        else if((method.getName()).equals(body.getMethod().getName())){
                            continue;
                        }
                        VirtualInvokeExpr virtualInvokeExpr = (VirtualInvokeExpr) invokeExpr;
                        Local baseLocal = (Local) virtualInvokeExpr.getBase(); // Cast to Local, because the base is a local variable

                        int f=0;
                        String staticType=null;
                        Set<String> set=variableToPointedTypes.get(baseLocal.toString());
                        if(set.size()==1){
                            f=1;
                            staticType=set.iterator().next();
                        }
                       
                       
                        if(f==1){
                        // Check if the type of the base object is DerivedClass2
                        // Create a new SpecialInvokeExpr
                            SootClass staticClass = Scene.v().getSootClass(staticType);
                            List<SootClass> immutableSuperClasses=Scene.v().getActiveHierarchy().getSuperclassesOf(staticClass);
                            List<SootClass> superClasses = new ArrayList<>(immutableSuperClasses);
                            String methodSignature = virtualInvokeExpr.getMethod().getSubSignature(); 
                            superClasses.add(0, staticClass);
                            
                            String className=null;
                            for(SootClass s:superClasses){
                                boolean hasMethod=false;
                                hasMethod = s.declaresMethod(methodSignature);
                                if(hasMethod==true){
                                    className=s.getName();
                                    staticType=s.getName();
                                    break;
                                }
                            }
                           
                            
                            SootClass baseClass = Scene.v().getSootClass(className);
                           
                            SootMethod originalMethod = baseClass.getMethod(methodSignature); // void foo()
                           
                            if (originalMethod.isConcrete() && originalMethod.hasActiveBody()) {
                                String staticMethodName = virtualInvokeExpr.getMethod().getName();  // This gives you "foo"
                                List<Type> paramTypes=new ArrayList<>(originalMethod.getParameterTypes());
                                paramTypes.add(0,Scene.v().getRefType(staticType));
                                String newMethod=staticMethodName+"1";
                                // Create a new static method with the same parameters and return type
                                SootClass sootClass = Scene.v().getSootClass(staticType);
                                
                                SootMethod staticMethod;
                                
                            if(!methodExists(sootClass, newMethod, paramTypes)){
                                 staticMethod = new SootMethod(
                                                                       newMethod,
                                                                        paramTypes,
                                                                        originalMethod.getReturnType(),
                                                                        Modifier.PUBLIC | Modifier.STATIC
                                                                    );

                                baseClass.addMethod(staticMethod);

                                JimpleBody newBody = Jimple.v().newBody(staticMethod);
                                staticMethod.setActiveBody(newBody);
                                Body originalBody = originalMethod.getActiveBody();
                                
                                

                                // Map to keep track of old locals to new locals
                                Map<Value, Value> localMap = new HashMap<>();
                                for (Local originalLocal : originalBody.getLocals()) {
                                Local clonedLocal = Jimple.v().newLocal(originalLocal.getName(), originalLocal.getType());
                                newBody.getLocals().add(clonedLocal);
                                localMap.put(originalLocal, clonedLocal);
                                }
                                int paramChanged=0;
                                for (Unit unit : originalBody.getUnits()) {
                                    
                                    if (unit instanceof Stmt && !(unit instanceof IdentityStmt)) {
                                        Stmt stmt1 = (Stmt) unit;
                                        Stmt clonedStmt = (Stmt) stmt1.clone();
                                        for (ValueBox box : clonedStmt.getUseAndDefBoxes()) {
                                            Value value = box.getValue();
                                            if (value instanceof Local) {
                                                Local originalLocal = (Local) value;
                                                for (Local newLocal : newBody.getLocals()) {
                                                    if (newLocal.getName().equals(originalLocal.getName()) && newLocal.getType().equals(originalLocal.getType())) {
                                                        box.setValue(newLocal);
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                        
                                        newBody.getUnits().add(clonedStmt);
                                        oldToNewUnitMap.put(unit, clonedStmt);
                                    }
    
                                    else if (unit instanceof IdentityStmt) {
                                        IdentityStmt stmt1 = (IdentityStmt) unit;
                                        Stmt clonedStmt = (Stmt) stmt1.clone();
                                        Value rightOp = stmt1.getRightOp();
                                        if (rightOp instanceof ThisRef) {
                                            // Assuming parameter 0 is of the correct type, otherwise you need to find the correct index
                                            ParameterRef paramRef = Jimple.v().newParameterRef(((ThisRef) rightOp).getType(), 0);
                                            IdentityStmt newIdentityStmt = Jimple.v().newIdentityStmt(stmt1.getLeftOp(), paramRef);
                                            newBody.getUnits().add(newIdentityStmt);
                                            oldToNewUnitMap.put(unit, newIdentityStmt);
                                            paramChanged=1;
                                        }

                                        else if(rightOp instanceof ParameterRef){
                                            if(paramChanged==1){
                                                Value leftOp = stmt1.getLeftOp();
                                                Local local = (Local) leftOp;
                                                ParameterRef paramRef = (ParameterRef) rightOp;
                                                int index = paramRef.getIndex();  // Get the parameter index
                                                int newIndex =index+1;
                                                ParameterRef newParamRef = Jimple.v().newParameterRef(local.getType(), newIndex);
                                                IdentityStmt newStmt = Jimple.v().newIdentityStmt(local, newParamRef);

                                                newBody.getUnits().add(newStmt);
                                                oldToNewUnitMap.put(unit, newStmt);
                                            }
                                            else{
                                                newBody.getUnits().add(stmt1);
                                                oldToNewUnitMap.put(unit, stmt1);
                                            }
                                        }
                                    }
                                }
                                
                                for (Unit clonedUnit : newBody.getUnits()) {
                                    if (clonedUnit instanceof IfStmt) {
                                        IfStmt ifStmt = (IfStmt) clonedUnit;
                                        ifStmt.setTarget(oldToNewUnitMap.get(ifStmt.getTarget()));
                                    } else if (clonedUnit instanceof GotoStmt) {
                                        GotoStmt gotoStmt = (GotoStmt) clonedUnit;
                                        gotoStmt.setTarget(oldToNewUnitMap.get(gotoStmt.getTarget()));
                                    } 
                                    
                                }
                                

                                
    
                                SootMethod retrievedMethod = baseClass.getMethod(staticMethod.getSubSignature());
    
            
                            }
                            

                            else{
                                String returnType=originalMethod.getReturnType().toString();
                                String paramTypesString = paramTypes.stream()
                                                .map(Type::toString)
                                                .collect(Collectors.joining(","));
                                String subSignature =returnType + " " + newMethod + "(" + paramTypesString + ")";
                               
                                staticMethod = sootClass.getMethod(subSignature);
                                
                            }
                                List<Value> args = new ArrayList<>(virtualInvokeExpr.getArgs());
                                args.add(0, virtualInvokeExpr.getBase());  // Add the original receiver as the first argument
                                StaticInvokeExpr staticInvokeExpr = Jimple.v().newStaticInvokeExpr(
                                        staticMethod.makeRef(),
                                        args
                                );
                                
                          
                                // Create a new InvokeStmt or replace in an AssignStmt
                                if (stmt instanceof InvokeStmt) {
                                    InvokeStmt newInvokeStmt = Jimple.v().newInvokeStmt(staticInvokeExpr);
                                    units.swapWith(stmt, newInvokeStmt); // Replace the old stmt with the new one
                                } else if (stmt instanceof AssignStmt) {
                                    AssignStmt assignStmt = (AssignStmt) stmt;
                                    AssignStmt newAssignStmt = Jimple.v().newAssignStmt(
                                        assignStmt.getLeftOp(),
                                        staticInvokeExpr
                                    );
                                    units.swapWith(stmt, newAssignStmt); // Replace the old stmt with the new one
                                }                       
                            }
                        }
                    }
                }
            }
        }



   }
   
}



