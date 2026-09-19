
ALTER TABLE dbo.refresh_tokens
ALTER COLUMN created_at datetimeoffset(6) ;
ALTER TABLE dbo.refresh_tokens
ADD CONSTRAINT DF_createAt DEFAULT SYSDATETIMEOFFSET() FOR created_at;