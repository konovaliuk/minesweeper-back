package edu.mmsa.danikvitek.minesweeper
package util.exception

class BackendSecretNotSetError extends RuntimeException("JWT_SECRET was not set on the back end")