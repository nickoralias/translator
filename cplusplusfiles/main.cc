// Ankit goel's pretty printing 

#include <iostream>
#include "java_lang.h" 
#include "Header.h" 
#include <sstream> 
using namespace java::lang;


int main(int argc, char* argv[]) {

  __rt::Ptr<__rt::Array<String> > args = new __rt::Array<String>(argc -1);

  for ( int32_t i = 1; i < argc; i++) { 

     (*args)[i-1] = __rt::literal(argv[i]);

  } 

__Test1::main(args);

return 0;

}
