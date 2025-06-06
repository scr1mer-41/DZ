#pragma once

#include "iostream"

#include <unordered_map>
#include <string>
#include <vector>

class Error
{
private:
    void skip(const unsigned int position);
public:
    [[noreturn]] void error(const std::string msg);
    [[noreturn]] void lexError(const std::string msg);
    [[noreturn]] void syntaxError(const std::string msg);
    [[noreturn]] void contextError(const std::string msg);
};