ALTER TABLE public.applicationdetail
    ADD COLUMN IF NOT EXISTS structuredaifeedback jsonb;
