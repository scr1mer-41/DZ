#pragma once

#include "ovm.h"
#include "Item.h"

#include <iostream>
#include <unordered_map>
#include <string>
#include <vector>


class GenerateCode
{
public:
    GenerateCode();

    void gen(int cmd);
    void genConst(int constValue);
    void genVar(Item& item);
    void genFunc(std::string func);
    void genOperation(std::string operation);
    void genNegative();
    void genAddition();
    void genSubstraction();
    void genComparison(std::string operation);
    void NEGgenComparison(std::string operation);
    void genHalt(int exitCode);
    void genInInt();
    void genOutInt();
    void genOutLn();
    void genGoTo(int code);
    void fillGaps(int to);
    void fillAddress(int address, int value);


    void genDup();
    void genLoad();
    void genSTOP();
    void genDrop();
    void genAddress(Item& item);
    void genSave();

    int getCmdCounter();

    void runCode();
    void printCode();


private:
    int cmdCounter;
    OVM ovm;
};
