
	     _        _   _           __  __ _____ _____ 
	    / \   ___| |_(_)_   _____|  \/  |  ___|_   _|
	   / _ \ / __| __| \ \ / / _ \ |\/| | |_    | |  
	  / ___ \ (__| |_| |\ V /  __/ |  | |  _|   | |  
	 /_/   \_\___|\__|_| \_/ \___|_|  |_|_|     |_|  
                                                 
                                                 
     
# Open Source Managed File Transfer

The purpose of ActiveMFT is to provide a basic Managed File Transfer platform. It should be possible to setup file transfers based on polling schedules using a web based GUI or a rest-based interface for automation.

Every filetransfer-event should be logged in the gui.

It's still work in in progress and no working release is made yet!


# Architecture
ActiveMFT is a buffering middleware. I.e. a  file is delivered when the target system (FTP server or similar) is online.

The heart of ActiveMFT is a message broker, Apache Artemis. Every file received is saved as a message. Apache Artemis supports streaming large files without consuming much memory which is a key feature.

A number of message driven workers will then dispatch the files to the target system from the queue. If it fails, redelivery is attempted.


# Roadmap

### Protocols

The idea is to support the following protocols.

- File
- FTP
- SFTP
- "JMS"
- SAMBA
- WebDAV

# Frameworks used
- Spring boot backend
- Angular JS frontend.
- jhipster generator
- liquibase database model


