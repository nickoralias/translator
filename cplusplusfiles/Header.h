// Ankit Goel's Header
#pragma once

#include <stdint.h>
#include <string>

using namespace java::lang;
// Foward Declarations 

struct __A;

struct __A_VT;

typedef __rt::Ptr<__A> A;

struct __A { 

    // The data layout for java.lang.plainClassName
      __A_VT* __vptr;


     // The Constructor

          __A(); 
    // The instance methods of java.lang.plainClassName
    static A init_Construct( A); 
    static String toString( A);

    // The Function returning the class Object representing java.lang.plainClassName 
    static Class __class(); 
    static void init(  __A*  );

    static __A_VT __vtable;

 };

struct __A_VT{
    Class __isa;
    void (*__delete)(__A*);
    int32_t (*hashCode)(A);
    bool (*equals)(A , Object);
    Class (*getClass)(A);
    String (*toString) (A);


    __A_VT()
    : __isa(__A::__class()),
    __delete(&__rt::__delete<__A>),
      hashCode((int32_t(*)(A))&__Object::hashCode),
      equals((bool(*)(A , Object)) &__Object::equals), 
      getClass((Class(*)(A)) &__Object::getClass), 
      toString(&__A::toString) {
    }
};



struct __Test1;

struct __Test1_VT;

typedef __rt::Ptr<__Test1> Test1;

struct __Test1 { 

    // The data layout for java.lang.plainClassName
      __Test1_VT* __vptr;


     // The Constructor

          __Test1(); 
    // The instance methods of java.lang.plainClassName
    static Test1 init_Construct( Test1); 

    // The Function returning the class Object representing java.lang.plainClassName 
    static Class __class(); 
    static void init(  __Test1*  );

    static void main(__rt::Ptr<__rt::Array<String> > args);
    static __Test1_VT __vtable;

 };

struct __Test1_VT{
    Class __isa;
    void (*__delete)(__Test1*);
    int32_t (*hashCode)(Test1);
    bool (*equals)(Test1 , Object);
    Class (*getClass)(Test1);
    String (*toString) (Test1);


    __Test1_VT()
    : __isa(__Test1::__class()),
    __delete(&__rt::__delete<__Test1>),
      hashCode((int32_t(*)(Test1))&__Object::hashCode),
      equals((bool(*)(Test1 , Object)) &__Object::equals), 
      getClass((Class(*)(Test1)) &__Object::getClass), 
      toString((String(*)(Test1)) &__Object::toString) { 
    }
};



