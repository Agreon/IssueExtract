# Issue-Extract

Extracts Issues from source-code and dynamically adds and deletes them from the remote github-repository

## Installation
+ Go to Api-Tokens
+ Create a config.ie-file with content: "token <your_token>"

## Usage
./IssueExtract <TargetDirectory> <Params>

**Params**
+ '-r = bool' remove issues from repo when deleted in code. Standart = true 

## Syntax
Git-Issue: <Title> >> <Body> << [ labe1, label2 ]

/**
	Git-Issue: { 
	Title
	 >> Body << 
	[ label1, label2]
 }

**/


TODO
    + Labels

## Output-Design
+ Title
+ Body 
	+ IssueText
	+ Link zu Position in Code
	+ Vlt. mit Methode oder Klasse beim nächsten
		=> Einfach die Zeile Danach
	
TODO:

HowTo Multiline
+ if found Git-Issue 
	+ search until ] found, that is not in >><<
	+ add all these lines up



intellij-plugin mit id-inc?
