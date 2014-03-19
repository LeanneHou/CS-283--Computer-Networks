
Commands:
	REGISTER
	NAME (ID) (Name)
	JOIN (ID) (groupName)
	MSG (ID) (groupName) (msg)
	POLL (ID)
	UNREGISTER
	SHUTDOWN

Example:

Client: 	REGISTER
Back: 		(ACK) REGISTERED (ID)
Client:		(ACK)
Client:		NAME (ID) (Name)
Back: 		+SUCCESS: Hi (Name)!
Client:		(ACK)
Client: 	JOIN (ID) (groupName)
Back: 		+SUCCESS: Joined group (groupName)!
Client:		(ACK)
Client:		MSG (ID) (groupName) (msg)
Client:		POLL (ID)
Back: 		(ACK) +SUCCESS: FROM (Name) TO (groupName): (msg)
Client: 	(ACK)
Client: 	UNREGISTER
Back:		(ACK) UNREGISTERED
Client: 	(ACK)
Client: 	SHUTDOWN


Some logging:

nc -u localhost 20000
REGISTER
ACK40631 REGISTERED 31136
ACK40631
NAME 31136 Bob
ACK91828 +SUCCESS: Hi Bob!
ACK91828
JOIN 31136 Chat
ACK22991 +SUCCESS: Joined group Chat!
ACK22991
MSG 31136 Chat Hi, I'm Bob!
POLL 31136
ACK9888 +SUCCESS: FROM Bob TO Chat: Hi, I'm Bob!
ACK9888
POLL 31136
ACK47815 +SUCCESS: NO MESSAGE
ACK47815

// after another client connected
POLL 31136
ACK21951 +SUCCESS: FROM LISA TO Chat: Hi, I'm Lisa!
ACK21951
UNREGISTER
ACK69281 UNREGISTERED
ACK69281
SHUTDOWN
