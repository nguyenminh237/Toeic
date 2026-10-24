import { useState } from "react";

export function useForm(initialValues) {
  const [values, setValues] = useState(initialValues);
  const [errors, setErrors] = useState({});

  function handleChange(event) {
    const { name, value } = event.target;
    setValues((current) => ({ ...current, [name]: value }));
    setErrors((current) => ({ ...current, [name]: undefined }));
  }

  function setField(name, value) {
    setValues((current) => ({ ...current, [name]: value }));
  }

  function reset(nextValues = initialValues) {
    setValues(nextValues);
    setErrors({});
  }

  return { values, errors, setErrors, handleChange, setField, reset };
}
