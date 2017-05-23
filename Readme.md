# Issue-Extract

Extracts Issues from source-code and dynamically adds and deletes them from the remote github-repository

## Syntax
Git-Issue: <Title> >> <Body> <<

/** Git-Issue 'SomeTitle'
	'SomeText'
	['Label1','label2']
**/

TODO
    + Labels

## Output-Design
+ Title
+ Body 
	+ IssueText
	+ Position in Project (File, Row)
	+ Vlt. mit Methode oder Klasse beim nächsten
		=> Einfach die Zeile Danach

TODO:
intellij-plugin mit id-inc?