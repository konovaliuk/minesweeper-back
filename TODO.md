# Model

- User
  - user id
  - username
  - password hash
  - salt
  - rating
- Game
  - game id 
  - user id
  - width
  - height
  - state
    1. in progress
    2. won
    3. lost
  - cells
    - y
    - y
    - isMined
    - isFlagged
    - isDiscovered