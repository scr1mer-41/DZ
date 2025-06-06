#pragma once


#include <unordered_map>
#include <string>
#include <memory>
#include <vector>

#include "Error.h"
#include "Scanner.h"
#include "TableOfName.h"
#include "generateCode.h"

class Parser
{
public:
    Parser(Scanner& scanner, std::shared_ptr<Error> errorPtr);
    void compile();

private:
    Scanner scanner;
    std::shared_ptr<Error> errorPtr;
    TableOfName table;
    GenerateCode generateCode;

    void modulePrc();
    void importPrc();
    void sequenceDeclarationsPrc();
    void sequenceStatementsPrc();
    void constDeclarationPrc();
    int constExpresionPrc();
    void varDeclarationPrc();
    void statementsPrc();
    void variableOrCallPrc();
    void typePrc();
    void ifStatementPrc();
    void whileStatementPrc();
    void parameterPrc();
    Item::ItemTypes expressionPrc();
    Item::ItemTypes NEGexpressionPrc();
    Item::ItemTypes simpleExpressionPrc();
    Item::ItemTypes termPrc();
    Item::ItemTypes multiplierPrc();

    void loccateVariables();

    void contextImportPrc();
    void contextVarPrc();
    void checkProcParameters(Item item);
    void checkFuncParameters(Item item);

    void forStatementPrc();
    void repeatStatementPrc();

    void checkLex(Scanner::Lex lex);
    void errorIfNotExpectedLex(Scanner::Lex lex);
    void checkIntType(Item::ItemTypes type);
    void checkBoolType(Item::ItemTypes type);
    void errorIsNotVariable();
};

