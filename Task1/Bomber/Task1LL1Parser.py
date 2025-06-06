class LL1Parser:
    def __init__(self):
        self.table = [
            (),
            ("EOF", 0, 0,0,0,0),
            ("+",2,0,0,1,0),
            ("-",3,0,0,1,0),
            ("d",8,0,0,0,0),
            ("l",11,0,0,0,0),         # 5
            ("(",14,0,0,0,0),
            ("any",0,0,0,0,0),        # 7
            ("d",17,0,1,0,0),
            ("any",35,0,1,0,0),
            ("any",16,0,0,0,0),                  #10
            ("l",25,0,1,0,0),
            ("any",35,0,1,0,0),
            ("any",16,0,0,0,0),
            ("(",2,0,1,1,0),
            (")",16,1,0,1,0),                       #15
            ("any",0,0,0,0,0),
            ("d",42,1,1,0,0),
            (".",42,0,1,1,0),
            ("any",0,0,0,0,0),
            ("l",21,1,0,1,0),                       #20
            ("l",21,0,0,1,0),
            ("d",22,0,0,1,0),
            ("l",21,0,0,0,0),
            ("any",0,0,0,0,0),
            ("l",26,1,0,0,0),                       #25
            ("l",20,0,1,0,0),
            ("(",29,0,0,0,0),
            ("any",0,0,0,0,0),
            ("(",31,1,1,1,0),
            (")",28,1,0,1,0),                       #30
            ("any",32,0,0,0,0),
            ("any",2,0,1,0,0),
            (",",31,0,0,1,0),
            ("any",0,0,0,0,0),
            ("+",2,0,0,1,0),                       #35
            ("-",2,0,0,1,0),
            ("*",2,0,0,1,0),
            ("/",2,0,0,1,0),
            ("%",2,0,0,1,0),
            ("^",2,0,0,1,0),                       #40
            ("any",0,0,0,0,0),
            ("d",43,1,0,0,0),
            ("d",43,0,0,1,0),
            ("any",0,0,0,0,0),
        ]
        self.chars = []
        self.stack = []
        self.tokens = []
        self.funcs = []

        self.state = 1
        self.pos = 0
        self.current_char = ''

    def parse(self, text):
        self.chars = [x for x in text if x != " "]
        self.stack = []
        self.state = 1
        self.funcs = []
        self.tokens = []

        self.pos = 0
        self.current_char = self.chars[self.pos]

        while True:
            if (self.state == 0) and (not self.stack):
                break
            elif (self.state == 0) and (self.stack):
                self.state = self.stack.pop()+1
                continue

            if self.state == 7:
                self.error(f"unknown symbol '{self.current_char}' at position {self.pos+1}, expression expected")

            if not self.coincidence():
                if self.table[self.state][2] == 1:
                    self.error(f"at position {self.pos+1} expected {self.table[self.state][0]}.")
                self.go_next_state()
                continue
            else:

                if self.state == 20:
                    self.tokens.append(Token("IDENT", self.current_char, self.pos))
                elif self.state in (21, 22):
                    self.tokens[-1].value += self.current_char
                elif self.state == 17:
                    self.tokens.append(Token("NUMBER", "", self.pos))
                elif self.state in (43, 18):
                    self.tokens[-1].value += self.current_char
                elif self.state in range(35,41):
                    self.tokens.append(Token("OPERATOR", self.current_char, self.pos))
                elif self.state == 33:
                    self.funcs[-1].args_for_func += 1
                    self.tokens.append(Token("COMMA", self.current_char, self.pos))
                elif self.state == 19:
                    self.tokens[-1].value = float(self.tokens[-1].value)
                elif self.state in (29, 14):
                    if self.state == 29:
                        self.tokens[-1].type = "FUNC"
                        self.tokens[-1].args_for_func += 1
                        self.funcs.append(self.tokens[-1])
                    self.tokens.append(Token("LPAREN", self.current_char, self.pos))
                elif self.state in (15, 30):
                    self.tokens.append(Token("RPAREN", self.current_char, self.pos))
                elif self.state in (2, 3):
                    self.tokens.append(Token("U_OPERATOR", self.current_char, self.pos))


                if self.table[self.state][3] == 1:
                    self.stack.append(self.state)
                if self.table[self.state][4] == 1:
                    self.next_char()
                self.state = self.table[self.state][1]
        print("Выражение верно.")



        return self.tokens

    def error(self, msg):
        raise Exception(f"Parse error: {msg}")

    def coincidence(self):
        if self.table[self.state][0] == "any":
            return True
        elif self.current_char.isdigit() and (self.table[self.state][0] == "d"):
            return True
        elif self.current_char.isalpha() and (self.table[self.state][0] == "l") and (self.current_char != "EOF"):
            return True
        elif self.current_char == self.table[self.state][0]:
            return True
        return False

    def next_char(self):
        if self.pos < (len(self.chars) - 1):
            self.pos += 1
            self.current_char = self.chars[self.pos]
        else:
            self.pos += 1
            self.current_char = "EOF"

    def go_next_state(self):
        self.state += 1

class Token:
    def __init__(self, type, value, position):
        self.type = type
        self.value = value
        self.position = position
        self.args_for_func = 0
    def __repr__(self):
        return f'Token(type: {self.type}, value: {self.value}, position: {self.position}, args_for_func: {self.args_for_func})'

class Dictionary:
    def __init__(self):
        self.priority = {
            '(':0,
            ')':1,
            '+':2,
            '-':2,
            '*':3,
            '/':3,
            'u-':4,
            '^':5
        }
    def getPriority(self, char):
        return self.priority.get(char)

class PostFixConvertor:
    def __init__(self):
        self.dictionary = Dictionary()
        self.stack = []
        self.output = []

    def getPostFixString(self, ParsedTokens):
        self.stack = []
        self.output = []

        tokens = ParsedTokens
        i = 0
        n = len(tokens)
        while i < n:
            token = tokens[i]
            if (token.value in ('+', '-')) and ((i == 0) or (tokens[i-1].type in ('OPERATOR', 'LPAREN', 'COMMA'))):
                token.value = 'u' + token.value

            if token.type == 'NUMBER':
                self.output.append(token)
            elif (token.type == 'IDENT') and ((i+1 < n) and (tokens[i+1].type != 'LPAREN')):
                self.output.append(token)
            elif (token.type == 'FUNC') and ((i+1 < n) and (tokens[i+1].type == 'LPAREN')):
                self.stack.append(token)
            elif token.type == 'LPAREN':
                self.stack.append(token)
            elif token.type == "COMMA":
                while (self.stack[-1].type != 'LPAREN'):
                    self.output.append(self.stack.pop())
            elif token.type == 'RPAREN':
                while (self.stack) and (self.stack[-1].type != 'LPAREN'):
                    self.output.append(self.stack.pop())
                self.stack.pop()
            elif token.type == 'OPERATOR':
                while ((len(self.stack) != 0) and ((self.stack[-1].type in ('IDENT', 'FUNC')) or (self.dictionary.getPriority(token.value) <= self.dictionary.getPriority(self.stack[-1].value)))):
                    self.output.append(self.stack.pop())
                self.stack.append(token)
            i += 1

        while self.stack:
            self.output.append(self.stack.pop())

        return self.output

class Evaluate:
    def __init__(self):
        self.tokens = []
        self.stack = []

    def limit_on_number(self, number):
        for i in str(number).split("."):
            if len(i) >= 17:
                self.Error(f"Число {number} превысило ограничение в 16 цифр в целой части или в остатке.")

    def Error(self, msg):
        raise Exception(f'Calculation error: {msg}')

    def Calc(self, token):
        result = 0

        if token.type == 'OPERATOR':
            if token.value in ('+', '-', '*', '/', '^'):
                b = self.stack.pop()
                a = self.stack.pop()
                operator = token.value
                if operator == '+':
                    self.stack.append(a+b)
                elif operator == '-':
                    self.stack.append(a-b)
                elif operator == '*':
                    self.stack.append(a*b)
                elif operator == '/':
                    self.stack.append(a/b)
                elif operator == '^':
                    self.stack.append(a**b)
            elif token.value == 'u-':
                a = self.stack.pop()
                self.stack.append(-a)
        elif token.type == 'IDENT':

            res = float(input(f"Введите значение переменной {token.value}: "))
            self.limit_on_number(res)
            self.stack.append(res)
        elif token.type == 'FUNC':
            args = '('
            n = token.args_for_func
            for i in range(n-1,-1,-1):
                if i != 0:
                    args += str(self.stack[-1-i]) + ','
                else:
                    args += str(self.stack[-1-i]) + ')'
            for i in range(n):
                self.stack.pop()

            function = token.value + args

            res = float(input(f"Введите значение функции {function}: "))
            self.limit_on_number(res)

            self.stack.append(float(res))
        elif token.type == 'NUMBER':
            self.limit_on_number(token.value)
            self.stack.append(token.value)

    def evaluate(self, PostFixArrayOfTokens):
        self.tokens = PostFixArrayOfTokens
        self.stack.clear()

        for token in self.tokens:
            self.Calc(token)
        return self.stack.pop()

text = input("Введите выражение:")

try:
    parser = LL1Parser()
    parsedTokens = parser.parse(text)

    postfix = PostFixConvertor()
    postfixTokens = postfix.getPostFixString(parsedTokens)

    print(postfixTokens)

    evaluate = Evaluate()
    print(f"Ответ: {evaluate.evaluate(postfixTokens)}")

except Exception as e:
    print(e)