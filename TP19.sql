commit;

select * from donnees;
DECLARE
  X CHAR(200);
BEGIN
  X := 'A';

  LOCK_LIGNE(
    X => X
  );
--rollback; 
END;

DECLARE
  X CHAR(200);
BEGIN
  X := 'B';

  LOCK_LIGNE(
    X => X
  );
--rollback; 
END;

select * from donnees;
commit;

update donnees set valeur=valeur+1 where nom='A';

----------- 
-- lock table  nowait;
-----------

