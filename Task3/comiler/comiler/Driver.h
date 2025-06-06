#pragma once

#include <unordered_map>
#include <iostream>
#include <string>
#include <vector>

#include "Error.h"

class Driver
{
public:
    Driver(std::shared_ptr<Error> errorPtr);

    static char ch;
    static unsigned int line;
    static unsigned int position;
    static char chSpace;
    static char chTab;
    static char chEOL;
    static char chEOT;

    Driver() = default;
    void resetText(char const* path);
    void nextCh();

private:
    static std::vector<char> inputChain;
    std::shared_ptr<Error> errorPtr;
};