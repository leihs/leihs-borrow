class Hash
  def delete_in!(*path, key)
    self.dig(*path).try(:delete, key)
  end
end
