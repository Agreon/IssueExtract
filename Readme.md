// Git-Issue: 

Ordnerpfad angeben

Clientseitig?

intellij-plugin mit id-inc?

Daten aus .git/config

Syntax: 

// Git-Issue: <Title> <IssueText> [<Labels>]
// Git-Issue '<Title>': '<IssueText>' ['Label1','Label2']

/** Git-Issue 'SomeTitle'
	'SomeText'
	['Label1','label2']
**/

# Design
+ Title
+ Body 
	+ IssueText
	+ Position in Project (File, Row)
	+ Vlt. mit Methode oder Klasse beim nächsten
		=> Einfach die Zeile Danach
	

# Architektur
+ Parser
	-> Statemachine
	-> Beim durchlaufen bei jedem state-accept überprüfen welcher state-type es ist und dementsprechend zuordnen
	-> StartState -> [// | /* ] commentstart -> [Git-issue] IssueStart | [\n || */] EndState 
	-> IssueStart ' -> Title -> ['] TitleEnd -> 
	
