// Ankit goel's pretty printing
#include "java_lang.h" 
#include "Header.h" 
#include <sstream> 
using namespace java::lang;

 Class __A::__class() { 
    static Class k  = 
    new __Class(__rt::literal("java.lang.A") , __Object::__class());
    return k; 
 }

String  __A::toString(A __this ) { 
   std::ostringstream sout;
   sout <<"A";
   return new __String(sout.str());
}


__A::__A() : __vptr(&__vtable) {
}

__A_VT __A:: __vtable;

A __A::init_Construct(A __this ) {


    return __this; 
 } 

 // Array Specialization for Java Classes 

 namespace __rt { 
 template<>
 java::lang::Class Array<A>::__class() {
 static java::lang::Class k = 
 new java::lang::__Class(literal("[LA;"),
                         java::lang::__Object::__class(),
                         __A::__class());
 return k; 
 }
 }


 Class __Test1::__class() { 
    static Class k  = 
    new __Class(__rt::literal("java.lang.Test1") , __Object::__class());
    return k; 
 }


void __Test1::main(__rt::Ptr<__rt::Array<String> > args) { 
    A  a = __A::init_Construct(  new __A());
    std::cout << a->__vptr->toString(a) << std::endl; 
}


__Test1::__Test1() : __vptr(&__vtable) {
}

__Test1_VT __Test1:: __vtable;

Test1 __Test1::init_Construct(Test1 __this ) {


    return __this; 
 } 

 // Array Specialization for Java Classes 

 namespace __rt { 
 template<>
 java::lang::Class Array<Test1>::__class() {
 static java::lang::Class k = 
 new java::lang::__Class(literal("[LTest1;"),
                         java::lang::__Object::__class(),
                         __Test1::__class());
 return k; 
 }
 }


