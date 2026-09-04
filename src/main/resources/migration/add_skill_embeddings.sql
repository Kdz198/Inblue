CREATE EXTENSION IF NOT EXISTS vector;

ALTER TABLE public.jobdescription
    ADD COLUMN IF NOT EXISTS skill_embedding vector(384);

ALTER TABLE public.usercareerpreference
    ADD COLUMN IF NOT EXISTS skill_embedding vector(384);
