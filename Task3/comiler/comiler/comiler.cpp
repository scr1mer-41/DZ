#include <iostream>
#include "Driver.h"
#include "Parser.h"
#include "Error.h"
#include "Scanner.h"
#include "ovm.h"
#include "generateCode.h"

#include <unordered_map>
#include <string>
#include <vector>
#include <locale>

int main()
{
    setlocale(LC_ALL, "Russian");
    std::cout << "Компилятор языка O" << std::endl;
    std::shared_ptr<Error> errorPtr = std::make_shared<Error>();
    Driver driver(errorPtr);

    //if (argc < 2)
    //{
    //    errorPtr->error("В качестве аргумента должен быть указан файл *.o");
    //}

    //driver.resetText(argv[1]);
    
    driver.resetText("program.txt");

    Scanner scanner(driver, errorPtr);
    Parser parser(scanner, errorPtr);
    parser.compile();

    std::cout << "Скомпилировано";

    // OVM ovm;
    // ovm.printCode();
    // ovm.test_input();
    // ovm.run();


    return 0;
}
